package com.rw.websocket.infre.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("random-world.websocket")
public class ApplicationProperties {

    private String coreUrl = "http://127.0.0.1:8080";
    private Long newFishPrice = 200L;
    private String moneyName = "灵石";
    private Long workId = 1L;
    private Long centerId = 1L;
    private NettyProperties netty = new NettyProperties();

    public NettyProperties getNetty() {
        return netty;
    }

    public void setNetty(NettyProperties netty) {
        this.netty = netty;
    }

    public String getCoreUrl() {
        return coreUrl;
    }

    public void setCoreUrl(String coreUrl) {
        this.coreUrl = coreUrl;
    }

    public Long getWorkId() {
        return workId;
    }

    public void setWorkId(Long workId) {
        this.workId = workId;
    }

    public Long getCenterId() {
        return centerId;
    }

    public void setCenterId(Long centerId) {
        this.centerId = centerId;
    }

    public Long getNewFishPrice() {
        return newFishPrice;
    }

    public void setNewFishPrice(Long newFishPrice) {
        this.newFishPrice = newFishPrice;
    }

    public String getMoneyName() {
        return moneyName;
    }

    public void setMoneyName(String moneyName) {
        this.moneyName = moneyName;
    }

    public static class NettyProperties {

        private IdleTimeoutProperties idleTimeout = new IdleTimeoutProperties();


        public IdleTimeoutProperties getIdleTimeout() {
            return idleTimeout;
        }

        public void setIdleTimeout(IdleTimeoutProperties idleTimeout) {
            this.idleTimeout = idleTimeout;
        }

        public static class IdleTimeoutProperties {

            private Boolean enabled = true;

            private Integer readTimeoutSeconds = 0;
            private Integer writeTimeoutSeconds = 0;
            private Integer allTimeoutSeconds = 12;

            public Integer getReadTimeoutSeconds() {
                return readTimeoutSeconds;
            }

            public void setReadTimeoutSeconds(Integer readTimeoutSeconds) {
                this.readTimeoutSeconds = readTimeoutSeconds;
            }

            public Integer getWriteTimeoutSeconds() {
                return writeTimeoutSeconds;
            }

            public void setWriteTimeoutSeconds(Integer writeTimeoutSeconds) {
                this.writeTimeoutSeconds = writeTimeoutSeconds;
            }

            public Integer getAllTimeoutSeconds() {
                return allTimeoutSeconds;
            }

            public void setAllTimeoutSeconds(Integer allTimeoutSeconds) {
                this.allTimeoutSeconds = allTimeoutSeconds;
            }

            public Boolean getEnabled() {
                return enabled;
            }

            public void setEnabled(Boolean enabled) {
                this.enabled = enabled;
            }
        }

    }

}
