var downloaderApp = new Vue({
    el: "#downloader-app",
    data: function() {
        return {
            errors: {},
            downloadMessage: null,
            loading: false,
            group: "com.adaptris",
            artifact: null,
            version: null
        };
    },
    computed: {
    },
    methods: {
        download: function(event) {
            var self = this;
            event.preventDefault();
            if (self.validate(event)) {
                self.loading = true;
                self.downloadMessage = "Resolving artifacts, this may take some time...";
                adp.ws.artifactResolve(self.group, self.artifact, self.version).done( function(data) {
                    self.downloadMessage = (data.dependencies.length + 1) + " artifacts resolved, the zip download will start shortly...";
                    adp.ws.artifactDownload(self.group, self.artifact, self.version);
                }).fail(function(error) {
                    self.downloadMessage = null;
                    // console.log(error);
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
            return this.getError(property) != null;
        }
    }
});
var starterApp = new Vue({
	el: "#starter-app",
	components: { "v-tags-input": VoerroTagsInput },
	data: function() {
		return {
			errors: {},
			generateMessage: null,
			loading: false,
			selectedArtifacts: null,
			version: null,
			existingArtifacts: [],
		};
	},
	computed: {
		artifacts: function(event) {
			var artifacts = [];
			for (var i = 0; i < this.selectedArtifacts.length; i++) {
				if (this.selectedArtifacts[i] && this.selectedArtifacts[i].value) {
					artifacts.push(this.selectedArtifacts[i].value);
				}
			}
			return artifacts.join(",");
		},
	},
	methods: {
		getArtifacts: function(event) {
			var self = this;
			self.loading = true;
			adp.ws.getArtifacts(self.version).done(function(data) {
				self.existingArtifacts = [];
				for (var i = 0; i < data.length; i++) {
					self.existingArtifacts.push({ key: i, value: data[i] });
				}
            }).fail(function(error) {
            	// console.log(error);
            }).always(function() {
                self.loading = false;
            });
		},
		generate: function(event) {
			var self = this;
			event.preventDefault();
			if (self.validate(event)) {
				self.loading = true;
				self.generateMessage = "Generating files, this may take some time...";
				adp.ws.starterGenerate(self.version, self.artifacts);
				self.loading = false;
				self.generateMessage = "";
			}
		},
		validate: function(event) {
			this.generateMessage = null;
			this.errors = {};
			if (this.artifacts && this.version) {
				return true;
			}
			if (!this.artifacts) {
				this.errors["artifacts"] = "Artifacts required.";
			}
			if (!this.version) {
				this.errors["version"] = "Version required.";
			}
		},
		getError: function(property) {
			return this.errors[property];
		},
		hasError: function(property) {
			return this.getError(property) != null;
		}
	}
});