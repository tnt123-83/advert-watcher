package com.group.util;

import com.group.dto.AdvertDto;
import javafx.application.HostServices;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class HyperlinkCell implements Callback<TableColumn<AdvertDto, Hyperlink>, TableCell<AdvertDto, Hyperlink>> {

    private static HostServices hostServices ;

    public static HostServices getHostServices() {
        return hostServices ;
    }

    public static void setHostServices(HostServices hostServices) {
        HyperlinkCell.hostServices = hostServices ;
    }

    @Override
    public TableCell<AdvertDto, Hyperlink> call(TableColumn<AdvertDto, Hyperlink> advertDtoHyperlinkTableColumn) {
        TableCell<AdvertDto, Hyperlink> cell = new TableCell<AdvertDto, Hyperlink>() {

            private final Hyperlink hyperlink = new Hyperlink();
            {
                hyperlink.setOnAction(event -> {
                    String url = getItem().getText();
                    hostServices.showDocument(url);
                });
            }

            @Override
            protected void updateItem(Hyperlink url, boolean empty) {
                super.updateItem(url, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    hyperlink.setText(url.getText());
                    setGraphic(hyperlink);
                }
            }
        };
        return cell;
    }
}