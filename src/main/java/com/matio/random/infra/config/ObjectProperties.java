package com.matio.random.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties("random-world")
public class ObjectProperties {

    private Map<String, ObjectInitProperties> objects = new HashMap<>();

    public Map<String, ObjectInitProperties> getObjects() {
        return objects;
    }

    public void setObjects(Map<String, ObjectInitProperties> objects) {
        this.objects = objects;
    }

    public static class ObjectInitProperties {
        private String topic;
        private Integer heal;
        private Integer atk;
        private Integer money;

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public Integer getHeal() {
            return heal;
        }

        public void setHeal(Integer heal) {
            this.heal = heal;
        }

        public Integer getAtk() {
            return atk;
        }

        public void setAtk(Integer atk) {
            this.atk = atk;
        }

        public Integer getMoney() {
            return money;
        }

        public void setMoney(Integer money) {
            this.money = money;
        }
    }


}
