package org.snomed.otf.reasoner.server.service;

import org.ihtsdo.otf.resourcemanager.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.module.storage.*;
import org.snomed.otf.owltoolkit.util.InputStreamSet;
import org.snomed.otf.reasoner.server.configuration.ApplicationProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Component
public class DependencyService {
	private static final Logger LOGGER = LoggerFactory.getLogger(DependencyService.class);

	private final ApplicationProperties applicationProperties;
	private final ResourceManager snomedReleaseResourceManager;
	private final RF2Service rf2Service;
	private final ModuleStorageCoordinator moduleStorageCoordinator;

	public DependencyService(ApplicationProperties applicationProperties, @Qualifier("legacyReleaseResourceManager") ResourceManager snomedReleaseResourceManager, RF2Service rf2Service, ModuleStorageCoordinator moduleStorageCoordinator) {
		this.applicationProperties = applicationProperties;
		this.snomedReleaseResourceManager = snomedReleaseResourceManager;
		this.rf2Service = rf2Service;
		this.moduleStorageCoordinator = moduleStorageCoordinator;
	}

	/**
	 * Return an InputStream collection storing given Delta's dependencies.
	 *
	 * @param previousPackages A collection of the given Delta's dependencies.
	 * @param deltaArchive     The Delta to process.
	 * @return An InputStream collection storing given Delta's dependencies.
	 * @throws IOException When an error occurs processing the InputStream collection.
	 */
	public InputStreamSet getInputStreamSet(Set<String> previousPackages, InputStream deltaArchive) throws IOException, ModuleStorageCoordinatorException.OperationFailedException, ModuleStorageCoordinatorException.ResourceNotFoundException, ModuleStorageCoordinatorException.InvalidArgumentsException {
		if (applicationProperties.isLegacyDependencyManagement()) {
			LOGGER.info("Loading dependencies using LEGACY technique (via given parameters)");
			close(deltaArchive);
			return viaGivenParameters(previousPackages);
		} else {
			LOGGER.info("Loading dependencies using NEW technique (via given MDRS)");
			return viaGivenMDRS(previousPackages,deltaArchive);
		}
	}

	// Legacy technique: this is deprecated and will be removed in a future release.
	private InputStreamSet viaGivenParameters(Set<String> previousReleases) throws IOException {
		Set<InputStream> previousReleaseStreams = new HashSet<>();
		try {
			for (String previousRelease : previousReleases) {
				previousReleaseStreams.add(snomedReleaseResourceManager.readResourceStream(previousRelease));
			}
		} catch (IOException e) {
			previousReleaseStreams.forEach(inputStream -> {
				try {
					inputStream.close();
				} catch (IOException closeException) {
					LOGGER.error("Failed to close stream.", closeException);
				}
			});
			throw e;
		}

		return new InputStreamSet(previousReleaseStreams.toArray(new InputStream[]{}));
	}

	// New technique: this is now the preferred way to load dependencies.
	private InputStreamSet viaGivenMDRS(Set<String> previousPackages, InputStream deltaArchive) throws FileNotFoundException, ModuleStorageCoordinatorException.OperationFailedException, ModuleStorageCoordinatorException.ResourceNotFoundException, ModuleStorageCoordinatorException.InvalidArgumentsException {
		Set<RF2Row> mdrs = rf2Service.getMDRS(deltaArchive, true);
		if (mdrs == null || mdrs.isEmpty()) {
			LOGGER.error("No MDRS given: cannot compute dependencies.");
			return null;
		}


		Set<ModuleMetadata> composition = moduleStorageCoordinator.getComposition(mdrs, true, getTransientSourceEffectiveTimes(previousPackages));
		if (composition == null || composition.isEmpty()) {
			LOGGER.error("No dependencies found");
			return null;
		}

		LOGGER.info("Dependent packages identified as: {}", composition.stream().map(ModuleMetadata::getFilename).toList());
		Set<InputStream> inputStreams = new HashSet<>();
		try {
			for (ModuleMetadata dependency : composition) {
				inputStreams.add(new FileInputStream(dependency.getFile()));
			}
			return new InputStreamSet(inputStreams.toArray(new InputStream[]{}));
		} catch (Exception e) {
			for (InputStream inputStream : inputStreams) {
				try {
					inputStream.close();
				} catch (Exception closeException) {
					LOGGER.error("Failed to close stream.", closeException);
					throw e;
				}
			}
			throw e;
		}
	}

	private void close(InputStream inputStream) {
		if (inputStream == null) {
			return;
		}

		try {
			inputStream.close();
		} catch (Exception e) {
			LOGGER.error("Failed to close InputStream", e);
		}
	}

	private Set<String> getTransientSourceEffectiveTimes(Set<String> previousPackages) throws ModuleStorageCoordinatorException.OperationFailedException, ModuleStorageCoordinatorException.ResourceNotFoundException, ModuleStorageCoordinatorException.InvalidArgumentsException {
        Map<String, List<ModuleMetadata>> releasesToCodeSystemMap = moduleStorageCoordinator.getAllReleases();
        List<ModuleMetadata> allReleases = new ArrayList<>();
		releasesToCodeSystemMap.values().forEach(allReleases::addAll);

		Set<String> transientSourceEffectiveTimes = new HashSet<>();
		for (String previousPackage : previousPackages) {
            allReleases.stream().filter(item -> item.getFilename().equals(previousPackage)).findFirst().ifPresent(foundModuleMetadata -> transientSourceEffectiveTimes.add(foundModuleMetadata.getEffectiveTimeString()));
        }

		return transientSourceEffectiveTimes;
	}
}
