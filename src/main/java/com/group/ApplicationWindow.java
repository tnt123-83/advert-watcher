package com.group;

import com.group.service.GenerateDataService;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class ApplicationWindow extends Application {
    private static ApplicationContext context;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void init() {
        context = new AnnotationConfigApplicationContext(JavaConfig.class);
        //System.out.println(context.getBean(GenerateDataService.class));
        //System.out.println(context.getBean(SpringStageLoader.class));
    }

    @Override
    public void start(Stage primaryStage) {
        context.getBean(SpringStageLoader.class).loadMain().show();
    }

    @Override
    public void stop() throws Exception {
        context.getBean(SpringStageLoader.class).saveParamChanges();
        super.stop();
    }
}
