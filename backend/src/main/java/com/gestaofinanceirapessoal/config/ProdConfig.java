package com.gestaofinanceirapessoal.config;

import com.gestaofinanceirapessoal.services.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;

@Configuration
@Profile("prod")
public class ProdConfig {

    @Autowired
    private DBService dbService;

    @EventListener(ApplicationReadyEvent.class)
    public void initDB(){
        this.dbService.initDB();
    }

}
