package pl.msocha.spacexrepository.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a rocket in the SpaceX system
 */
@Getter
public class Rocket {

		final String id;
		final String name;
		@Setter
		RocketStatus status;
		@Setter
		String missionId;

		public Rocket(String id, String name) {
				this.id = id;
				this.name = name;
				this.status = RocketStatus.ON_GROUND;
				this.missionId = null;
		}
}
