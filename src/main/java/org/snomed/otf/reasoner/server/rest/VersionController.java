package org.snomed.otf.reasoner.server.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.snomed.otf.reasoner.server.pojo.BuildVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Version", description = "Build Version")
public class VersionController {

	@Autowired(required = false)
	private BuildProperties buildProperties;

	@Operation(summary = "Software build version and build timestamp.")
	@RequestMapping(value = "/version", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public BuildVersion getBuildInformation() {
		return new BuildVersion(buildProperties.getVersion(), buildProperties.getTime().toString());
	}

}
