package pl.msocha.spacexrepository.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a summary of a Rocket
 */
@Getter
@AllArgsConstructor
public class RocketSummary {

		final String rocketName;
		final RocketStatus rocketStatus;
}
