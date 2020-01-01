package com.group.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group.domain.entity.Filter;
import com.group.domain.entity.Resource;
import com.group.dto.AdvertDto;
import com.group.service.GenerateDataService;
import com.group.util.Counter;
import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Data
public class GenerateDataServiceImpl implements GenerateDataService {
    @javax.annotation.Resource(name = "params")
    private Map<String, String> params;

    private final Counter latch = new Counter(0);
    private final ArrayDeque<AdvertDto> queue = new ArrayDeque<>();
    private final Set<AdvertDto> tempFoundAdverts = new HashSet<>();
    private final Set<AdvertDto> foundAdverts = new HashSet<>();

    private Boolean isWorkingTaskCancelled = false;

    public Boolean isDataTransmited() {
        return latch.getCount() < 1 && queue.isEmpty();
    }

    @Override
    public void setWorkingTaskCancelled(Boolean status) {
        isWorkingTaskCancelled = status;
    }

    public void runGenerator() {
        latch.countUp();
        queue.clear();
        List<Resource> resources = getResources();
        Thread task = new Thread("Task1") {
            @Override
            public void run() {
                for (Resource res : resources) {
                    if (isWorkingTaskCancelled) break;
                    parseAndFilterDataFromResourceWithParams(true, false, true, res);
                }
                //if (!isWorkingTaskCancelled) {
                    foundAdverts.clear();
                    foundAdverts.addAll(tempFoundAdverts);
                    tempFoundAdverts.clear();
                //}
                latch.countDown();
            }
        };
        task.start();
    }

    public ArrayDeque<AdvertDto> generateData() {
        synchronized (queue) {
            ArrayDeque<AdvertDto> partialResult = new ArrayDeque<>(queue);
            queue.clear();
            return partialResult;
        }
    }

    private List<Resource> getResources() {
        List<Filter> filters = parseFilters();
        List<Resource> resources = parseResources();
        joinFiltersWithResources(createIdToResourceMap(resources), filters);

        return resources;
    }

    private void joinFiltersWithResources(Map<Long, Resource> idToResourceMap, List<Filter> filters) {
        filters
                .forEach(f -> f.getResourceIds()
                        .forEach(resId -> Optional.ofNullable(idToResourceMap.get(resId))
                                .ifPresent(res -> res.getFilters().add(f))));
    }

    private Map<Long, Resource> createIdToResourceMap(List<Resource> resources) {
        return resources.stream()
                .collect(Collectors.toMap(res -> res.getId(), res -> res));
    }

    private List<Resource> parseResources() {
        byte[] mapData = new byte[0];
        List<Resource> resources = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            mapData = Files.readAllBytes(Paths.get("src/main/resources/resources.json"));
            resources = objectMapper.readValue(mapData, new TypeReference<List<Resource>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Array is: " + resources);
        return resources;
    }

    public List<String> getResultTablesNames() {
        return parseFilters().stream().map(f -> f.getGroupName()).collect(Collectors.toList());
    }

    private List<Filter> parseFilters() {
        byte[] mapData = new byte[0];
        List<Filter> filters = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            mapData = Files.readAllBytes(Paths.get("src/main/resources/filters.json"));
            filters = Arrays.asList(objectMapper.readValue(mapData, Filter[].class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Array is: " + filters);
        return filters;
    }

    private Boolean filterData(List<Filter> filters, AdvertDto advert) {
        for (Filter filter : filters) {
            if (!filter.getContains().isEmpty()) {
                if (doesValueSatisfyCondition("price", advert.getPrice(), filter.getContains().get("price"))
                        && doesValueSatisfyCondition("", advert.getTitle(), filter.getContains().get("title"))
                        && doesValueSatisfyCondition("", advert.getDescription(), filter.getContains().get("description"))
                        && doesValueSatisfyCondition("", advert.getText(), filter.getContains().get("text"))
                        && doesValueSatisfyCondition("", advert.getLocation(), filter.getContains().get("location"))) {
                    advert.setFilter(filter);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean doesValueSatisfyCondition(String type, String value, String condition) {
        boolean v = value == null || value.trim().isEmpty();
        //generate message val is empty
        boolean c = condition == null || condition.trim().isEmpty();
        if (v && !c) return false;
        if (!v && !c) {
            switch (type) {
                case ("price"):
                    return processDoesPriceSatisfyCondition(value.replaceAll(" ", ""), condition);
                default:
                    return processDoesTextSatisfyCondition(value, condition);
            }
        }
        return true;
    }

    private boolean processDoesPriceSatisfyCondition(String value, String condition) {
        String val = convertValues(params.get("exchange.usd"), "\\$", value.replaceAll(" ", ""));
        String cond = convertValues(params.get("exchange.usd"), "\\$", condition);
        BigDecimal number = new BigDecimal(val);
        if (cond.contains("..")) {
            String[] parts = condition.split("\\.\\.");
            return number.compareTo(new BigDecimal(parts[0])) >= 0 && number.compareTo(new BigDecimal(parts[1])) <= 0;
        }
        if (condition.contains(">")) {
            return number.compareTo(new BigDecimal(condition)) >= 0;
        }
        if (condition.contains("<")) {
            return number.compareTo(new BigDecimal(condition)) <= 0;
        }
        return false;
    }

    private String convertValues(String exchange, String type, String value) {
        String result = value;
        String[] parts = (value.trim() + " ").split(type);
        if (parts.length == 1) return result;
        result = "";
        Pattern p = Pattern.compile("\\D*(\\d+)\\D*");

        for (String part : parts) {
            if (part.trim().isEmpty()) continue;
            Matcher m = p.matcher(part);
            m.matches();
            String number = m.group(1);
            BigDecimal val = new BigDecimal(number);
            result += part.replace(number, val.multiply(new BigDecimal(exchange)).toString());
        }
        return result;
    }

    private boolean processDoesTextSatisfyCondition(String value, String condition) {
        if (condition.trim().startsWith("regexp: ")) {
            String regexp = condition.replace("regexp: ", "");
            Pattern p = Pattern.compile(regexp);
            Matcher m = p.matcher(value);
            return m.find();
        } else {
            String[] words = condition.toLowerCase().split(" ");
            String val = value.toLowerCase();
            for (String w : words) {
                if (!val.contains(w)) return false;
            }
        }
        return true;
    }

    private void parseAndFilterDataFromResource(Resource res) {
        parseAndFilterDataFromResourceWithParams(false, false, false, res);
    }

    private void parseAndFilterNewDataFromResource(Resource res) {
        parseAndFilterDataFromResourceWithParams(true, true, false, res);
    }

    private void parseAndFilterNewNotViewedDataFromResource(Resource res) {
        parseAndFilterDataFromResourceWithParams(true, true, true, res);
    }

    private void parseAndFilterDataFromResourceWithParams(boolean shouldSaveResults, boolean shouldReturnOnlyNewResults,
                                                          boolean shouldReturnNotViewedResults, Resource res) {
        Set<String> links = new HashSet<>();
        String link = res.getUrl();
        try {
            while (true) {
                if (isWorkingTaskCancelled) break;
                Document doc = Jsoup.connect(link).get();
                links.addAll(doc.select(res.getLocators().get("links"))
                        .stream()
                        .map(e -> res.getPrefix() + e.attr(res.getLocators().get("link-container")))
                        .collect(Collectors.toSet()));
                Element nextPage = doc.select(res.getLocators().get("nextPage")).first();
                if (nextPage != null && nextPage.hasAttr(res.getLocators().get("nextPage-container"))) {
                    link = res.getPrefix() + nextPage.attr(res.getLocators().get("nextPage-container"));
                } else {
                    break;
                }
            }
            for (String url : links) {
                if (isWorkingTaskCancelled) break;
                AdvertDto advert = generateAdvertByLink(url, res.getLocators());
                if (filterData(res.getFilters(), advert)) {
                    synchronized (queue) {
                        if (!isWorkingTaskCancelled) {
                            if (shouldSaveResults) tempFoundAdverts.add(advert);
                            if (shouldReturnOnlyNewResults) {
                                if (foundAdverts.contains(advert)) {
                                    continue;
                                }
                            }
                            if (shouldReturnNotViewedResults) {
                                foundAdverts.forEach(a -> {
                                    if (a.equals(advert)) advert.setViewed(a.getViewed());
                                });
                                //if (advert.getViewed().get()) continue;
                            }
                            queue.add(advert);
                        }
                    }
                }
            }
            System.out.println(queue);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private AdvertDto generateAdvertByLink(String link, Map<String, String> locators) {
        AdvertDto result = new AdvertDto();
        Document doc;
        try {
            doc = Jsoup.connect(link).get();
            result.setSiteId(parseValue(locators.get("siteId"), locators.get("siteId-container"), locators.get("siteId-details"), doc));
            //result.setSiteId(parseValue(locators.get("siteId"), locators.get("siteId-container"), locators.get("siteId-details"), doc));
            result.setUrl(link);
            result.setTitle(parseValue(locators.get("title"), locators.get("title-container"), locators.get("title-details"), doc));
            result.setDescription(parseValue(locators.get("description"), locators.get("description-container"), locators.get("description-details"), doc));
            result.setText(parseValue(locators.get("text"), locators.get("text-container"), locators.get("text-details"), doc));
            result.setPrice(parseValue(locators.get("price"), locators.get("price-container"), locators.get("price-details"), doc));
            //result.setDate(parseValue(locators.get("date), locators.get("date-container"), locators.get("date-details"), doc));
            result.setDate(new Date());
            result.setLocation(parseValue(locators.get("location"), locators.get("location-container"), locators.get("location-details"), doc));
            result.setFromAgent(parseValue(locators.get("fromAgent"), locators.get("fromAgent-container"), locators.get("fromAgent-details"), doc));
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String parseValue(String locator, String container, String details, Document doc) {
        String result = "";
        if (locator != null && !locator.isEmpty() && container != null && !container.isEmpty()) {
            if ("text".equalsIgnoreCase(container)) {
                result = doc.select(locator)
                        .stream()
                        .map(e -> e.text())
                        .findFirst().orElse("");
            } else {
                result = doc.select(locator)
                        .stream()
                        .map(e -> e.attr(container))
                        .findFirst().orElse("");
            }
            if (details != null && details.contains("regexp:") && !details.replace("regexp:", "").trim().isEmpty()) {
                Pattern p = Pattern.compile(details.replace("regexp:", "").trim());
                Matcher m = p.matcher(result);
                result = "";
                if (m.find()) {
                    for (int i = 1; i <= m.groupCount(); i++) {
                        result += m.group(i) + (m.groupCount() > 1 && i < m.groupCount() ? ", " : "");
                    }
                }
            }
        }

        return result;
    }

    public static void main(String[] args) {
        List<String> links = new ArrayList<>();


//        Document doc;
//        String result = "";
//        try {
//            doc = Jsoup.connect("https://www.olx.ua/obyavlenie/prodam-garazh-v-kooperative-zhiguli-IDFCHia.html#c44352ff12").get();
//            //doc = Jsoup.connect("https://besplatka.ua/obyavlenie/prodam-garazh-v-gk-bastion--klochkovskaya--naprotiv-617f3e").get();
//            //LocalDate date = LocalDate.now(ZoneId.of(src.getTimeZone()));
//
//            links = doc.select("div[class=offer-titlebox__details] a[class=show-map-link]")
//                    .stream()
//                    .map(e -> e.text())
//                    .collect(Collectors.toList());
//            result = links.get(0);
//            //Объявление от Частного лица Тип недвижимости Гараж Тип гаража Кирпичный Общая площадь 21 м² Комфорт Яма Стеллажи Охрана Продам свой приватизированный гараж в кооперативе "Жигули". Ширина 2.8м, глубина 5.1, высота 2.95м.
//            Pattern p = Pattern.compile(".*Объявление от (.*) Тип недвижимости.*".replace("regexp ", ""));
//            Matcher m = p.matcher(result);
//            m.find();
//            //result = m.group(1) + m.group(2);
//            Arrays.stream("2$..3$".split("\\$")).forEach(System.out::println);
//            System.out.println(m.groupCount());
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
