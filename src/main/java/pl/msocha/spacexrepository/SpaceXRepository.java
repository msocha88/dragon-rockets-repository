package pl.msocha.spacexrepository;

import java.util.Objects;
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

				if (isNullOrEmpty(rocketName)) {
						throw new IllegalArgumentException("Rocket name must not be empty");
				}

				var rocket = new Rocket("rocket" + rocketIdGenerator.getAndIncrement(), rocketName);
				rockets.put(rocket.getId(), rocket);
				return rocket.getId();
		}

		/**
		 * Creates and adds Mission to repository.
		 * @param missionName New Mission name
		 * @return Identifier of a created Mission
		 */
		public String addMission(String missionName) {

				if (isNullOrEmpty(missionName)) {
						throw new IllegalArgumentException("Mission name must not be empty");
				}

				var mission = new Mission("mission" + missionIdGenerator.getAndIncrement(), missionName);
				missions.put(mission.getId(), mission);
				return mission.getId();
		}

		/**
		 * Assigns Rocket to a Mission.
		 * Rocket status will change to IN_SPACE
		 * @param rocketId Identifier of a Rocket to assign
		 * @param missionId Identifier of a Mission to assign Rocket to
		 */
		public void assignRocketToMission(String rocketId, String missionId) {

				if (isNullOrEmpty(rocketId) || isNullOrEmpty(missionId)) {
						throw new IllegalArgumentException("Invalid rocketId or missionId");
				}

				var rocket = rockets.get(rocketId);
				var mission = missions.get(missionId);

				if (rocket == null) {
						throw new IllegalArgumentException("Rocket with id [%s] does not exists".formatted(rocketId));
				}
				if (mission == null) {
						throw new IllegalArgumentException("Mission with id [%s] does not exists".formatted(missionId));
				}

				if (MissionStatus.ENDED == mission.getStatus()) {
						throw new IllegalStateException("Mission is already ended");
				}

				if (rocket.getMissionId() != null) {
						throw new RuntimeException("Rocket is already assigned to mission");
				}

				mission.addRocket(rocket.getId());
				rocket.setMissionId(mission.getId());
				rocket.setStatus(RocketStatus.IN_SPACE);
		}

		/**
		 * Changest status of a Rocket.
		 * Changing Rocket status may affect status of Mission, that Rocket is assigned to.
		 * @param rocketId Identifier of Rocket to change status
		 * @param newStatus New status of a Rocket
		 */
		public void setRocketStatus(String rocketId, RocketStatus newStatus) {
				if (isNullOrEmpty(rocketId) || newStatus == null) {
						throw new IllegalArgumentException("Invalid rocketId or newStatus");
				}

				var rocket = rockets.get(rocketId);

				if (rocket == null) {
						throw new IllegalStateException("Rocket to change status is not in repository");
				}

				rocket.setStatus(newStatus);

				if (newStatus == RocketStatus.IN_REPAIR) {
						missions.get(rocket.getMissionId())
							.setStatus(MissionStatus.PENDING);
				}

		}

		/**
		 * Changest status of a Mission.
		 * @param missionId Identifier of Mission to change status
		 * @param newStatus New status of a Mission
		 */
		public void setMissionStatus(String missionId, MissionStatus newStatus) {
				if (isNullOrEmpty(missionId) || newStatus == null) {
						throw new IllegalArgumentException("Invalid missionId or newStatus");
				}

				var mission = missions.get(missionId);

				if (mission == null) {
						throw new IllegalStateException("Mission to change status is not in repository");
				}

				switch (newStatus) {
						case IN_PROGRESS -> validateInProgress(mission);
						case ENDED -> validateEnded(mission);
				}

				mission.setStatus(newStatus);
		}

		private void validateEnded(Mission mission) {
				if (!mission.getRocketIds().isEmpty()) {
						throw new IllegalStateException("Mission do not have still Rockets assigned");
				}
		}

		private void validateInProgress(Mission mission) {
				if (mission.getRocketIds().isEmpty()) {
						throw new IllegalStateException("Mission do not have any Rockets assigned");
				} else if (hasRocketsInRepair(mission)) {
						throw new IllegalStateException("In progress Mission can't have rockets wit IN_REPAIR status");
				}
		}

		private boolean hasRocketsInRepair(Mission mission) {
				return mission.getRocketIds()
					.stream()
					.map(rockets::get)
					.filter(Objects::nonNull)
					.map(Rocket::getStatus)
					.anyMatch(RocketStatus.IN_REPAIR::equals);
		}

		private static boolean isNullOrEmpty(String identifier) {
				return identifier == null || identifier.isEmpty();
		}

}
