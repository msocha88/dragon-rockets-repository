package pl.msocha.spacexrepository.model;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a mission in the SpaceX system
 */
@Getter
public class Mission {

		final String id;
		final String name;
		final Set<String> rocketIds;
		@Setter
		MissionStatus status;

		public Mission(String id, String name) {
				this.id = id;
				this.name = name;
				this.rocketIds = new HashSet<>();
				this.status = MissionStatus.SCHEDULED;
		}

		/**
		 * Adds new rocket to Mission
		 * @param rocketId id of added rocket
		 */
		public void addRocket(String rocketId) {
				rocketIds.add(rocketId);
		}
}
