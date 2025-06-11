package pl.msocha.spacexrepository;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import pl.msocha.spacexrepository.model.Mission;
import pl.msocha.spacexrepository.model.MissionStatus;
import pl.msocha.spacexrepository.model.Rocket;
import pl.msocha.spacexrepository.model.RocketStatus;

@RequiredArgsConstructor
public class SpaceXRepository {

		private final ConcurrentMap<String, Rocket> rockets;
		private final ConcurrentMap<String, Mission> missions;
		private final AtomicInteger rocketIdGenerator = new AtomicInteger(1);
		private final AtomicInteger missionIdGenerator = new AtomicInteger(1);

		/**
		 * Creates and adds Rocket to repository.
		 * @param rocketName New Rocket name
		 * @return Identifier of a created Rocket
		 */
		public String addRocket(String rocketName) {

				return null;
		}

		/**
		 * Creates and adds Mission to repository.
		 * @param missionName New Mission name
		 * @return Identifier of a created Mission
		 */
		public String addMission(String missionName) {

				return null;
		}

		/**
		 * Assigns Rocket to a Mission.
		 * Rocket status will change to IN_SPACE
		 * @param rocketId Identifier of a Rocket to assign
		 * @param missionId Identifier of a Mission to assign Rocket to
		 */
		public void assignRocketToMission(String rocketId, String missionId) {

		}

		/**
		 * Changest status of a Rocket.
		 * Changing Rocket status may affect status of Mission, that Rocket is assigned to.
		 * @param rocketId Identifier of Rocket to change status
		 * @param newStatus New status of a Rocket
		 */
		public void setRocketStatus(String rocketId, RocketStatus newStatus) {

		}

		/**
		 * Changest status of a Mission.
		 * @param missionId Identifier of Mission to change status
		 * @param newStatus New status of a Mission
		 */
		public void setMissionStatus(String missionId, MissionStatus newStatus) {

		}

}
