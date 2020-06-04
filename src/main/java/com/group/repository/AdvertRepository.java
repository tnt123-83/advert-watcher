package com.group.repository;

import com.group.domain.entity.Advert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdvertRepository extends JpaRepository<Advert, Long> {
    List<Advert> findByUrl(String url);

//    @Transactional
//    @Modifying
//    @Query(
//            value =
//                    "insert into Advert (SITE_ID, URL, TITLE, DESCRIPTION, TEXT, PRICE, DATE, LOCATION, FROM_AGENT, VIEWED, SAVE, GROUP_NAME) " +
//                            "values (:siteId, :url, :title, :description, :text, :price, :date, :location, :fromAgent, :viewed, :save, :groupName)",
//            nativeQuery = true)
//    void insert(@Param("siteId") String siteId, @Param("url") String url, @Param("title") String title, @Param("description") String description,
//                @Param("text") String text, @Param("price") String price, @Param("date") Date date, @Param("location") String location,
//                @Param("fromAgent") String fromAgent, @Param("viewed") boolean viewed, @Param("save") boolean save, @Param("groupName") String groupName);
}
