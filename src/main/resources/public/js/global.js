var adp = adp || {};

(function(adp) {
    adp.url = adp.url || {};
    
    function getContextPath() {
       if (window.location.protocol === "http:" || window.location.protocol === "https:") {
           return window.location.pathname.substring(0, window.location.pathname.indexOf("/ui",1));
       } else {
           return "/interlok-artifact-downloader";
       }
    }

    adp.url.context          = getContextPath();             // "/interlok-artifact-downloader"
    adp.url.api              = getContextPath() + "/api";
    adp.url.artifacts        = adp.url.api + "/artifacts";
    adp.url.starter          = adp.url.api + "/starter";
    
})(adp);

(function(adp) {
    adp.link = adp.link || {};
    
    adp.link.devBase         = "https://development.adaptris.net/";
    adp.link.dev             = adp.link.devBase + "index.html";
    adp.link.installers      = adp.link.devBase + "installers/Interlok/latest-stable/";
    adp.link.docBase         = adp.link.devBase + "docs/Interlok/";
    adp.link.adpBase         = "http://www.adaptris.com/";
    adp.link.aboutUs         = adp.link.adpBase + "pages/about-us/about-adaptris";
    adp.link.contactUs       = adp.link.adpBase + "pages/contact-adaptris";
    
})(adp);

(function(adp) {
    adp.ws = adp.ws || {};

    /* ************************* */
    /* *** Artifact Download *** */
    /* ************************* */
    adp.ws.artifactDownload = function(group, artifact, version) {
        window.location = adp.url.artifacts + "/" + group + "/" + artifact + "/" + version;
    };
    adp.ws.artifactResolve = function(group, artifact, version) {
        return $.ajax({
            url: adp.url.artifacts + "/" + group + "/" + artifact + "/" + version + "/resolve",
            type: "GET",
            dataType: "json",
            contentType: "application/json"
        });
    };
    adp.ws.getArtifacts = function(version) {
    	return $.ajax({
    		url: adp.url.artifacts + "/" + version,
    		type: "GET",
    		dataType: "json",
    		contentType: "application/json"
    	});
    };
    
    adp.ws.starterGenerate = function(version, artifacts) {
        window.location = adp.url.starter + "/generate/" + version + "?artifacts=" + artifacts;
    };
    
})(adp);

(function(adp) {
    adp.util = adp.util || {};

})(adp);