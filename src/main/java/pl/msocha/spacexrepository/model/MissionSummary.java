package pl.msocha.spacexrepository.model;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a summary of a Mission
 */
@Getter
@AllArgsConstructor
public class MissionSummary {

		final String missionName;
		final Set<RocketSummary> rockets;
		MissionStatus missionStatus;
}
