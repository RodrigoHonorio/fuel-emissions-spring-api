package uk.org.spire.emissionsCalculator.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StationApiResponse {

    @JsonProperty("Sites")
    private SitesWrapper sites;

    public SitesWrapper getSites() {
        return sites;
    }

    public void setSites(SitesWrapper sites) {
        this.sites = sites;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SitesWrapper {
        @JsonProperty("Site")
        private List<SiteDto> siteList;

        public List<SiteDto> getSiteList() {
            return siteList;
        }

        public void setSiteList(List<SiteDto> siteList) {
            this.siteList = siteList;
        }
    }
}