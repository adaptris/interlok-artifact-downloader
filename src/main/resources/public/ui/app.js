var app = new Vue({
  el: "#app",
  data: function () {
    return {
      errors: {},
      downloadMessage: null,
      loading: false,
      group: "com.adaptris",
      artifact: null,
      version: null
    }
  },
  computed: {
  },
  methods: {
    download: function (event) {
      var self = this;
      event.preventDefault();
      if (self.validate(event)){
        self.loading = true;
        self.downloadMessage = "Resolving artifacts, this may take some time...";
        adp.ws.artifactResolve(self.group, self.artifact, self.version).done(function(data) {
          self.downloadMessage = (data.dependencies.length + 1) + " artifacts resolved, the zip download will start shortly...";
          adp.ws.artifactDownload(self.group, self.artifact, self.version);
        }).fail(function(error) {
          self.downloadMessage = null;
          console.log(error);
          if (error && error.responseJSON) {
              self.errors["global"] = error.responseJSON.message;
          }
        }).always(function() {
          self.loading = false;
        });
      }
    },
    validate: function(event) {
      this.downloadMessage = null;
      this.errors = {};
      if (this.group && this.artifact && this.version) {
        return true;
      }
      if (!this.group) {
        this.errors["group"] = "Group required.";
      }
      if (!this.artifact) {
        this.errors["artifact"] = "Artifact required.";
      }
      if (!this.version) {
        this.errors["version"] = "Version required.";
      }
    },
    getError: function(property) {
        return this.errors[property];
    },
    hasError: function(property) {
      return this.getError(property) != undefined;
    }
  }
})