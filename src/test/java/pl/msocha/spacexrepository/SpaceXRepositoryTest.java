package pl.msocha.spacexrepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.assertj.core.api.Assertions.tuple;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import pl.msocha.spacexrepository.model.Mission;
import pl.msocha.spacexrepository.model.MissionStatus;
import pl.msocha.spacexrepository.model.Rocket;
import pl.msocha.spacexrepository.model.RocketStatus;

public class SpaceXRepositoryTest {

		ConcurrentMap<String, Rocket> rocketMap = new ConcurrentHashMap<>();
		ConcurrentMap<String, Mission> missionMap = new ConcurrentHashMap<>();

		SpaceXRepository tested = new SpaceXRepository(rocketMap, missionMap);

		@BeforeEach
		void beforeEach() {
				rocketMap.clear();
				missionMap.clear();
		}

		@Nested
		@DisplayName("Rocket Management Tests")
		class RocketManagementTests {

				@Test
				@DisplayName("Should create and store new Rocket")
				void shouldCreateAndStoreNewRocket() {

						//given
						var rocketName = "testRocket";

						//when
						var createdRocketId = tested.addRocket(rocketName);

						//then
						var storedRocket = rocketMap.get(createdRocketId);
						assertThat(storedRocket).isNotNull();
						assertThat(storedRocket.getId()).isNotNull();
						assertThat(storedRocket.getId()).isEqualTo(createdRocketId);
						assertThat(storedRocket.getName()).isEqualTo(rocketName);
						assertThat(storedRocket.getStatus()).isEqualTo(RocketStatus.ON_GROUND);
						assertThat(storedRocket.getMissionId()).isNull();
				}

				@ParameterizedTest
				@NullAndEmptySource
				@DisplayName("Should throw exception on invalid Rocket name")
				void shouldThrowExceptionOnInvalidRocketName(String rocketName) {
						//when
						var exception = catchException(() -> tested.addRocket(rocketName));

						//then
						assertThat(exception).isInstanceOf(IllegalArgumentException.class);
						assertThat(exception.getMessage()).isEqualTo("Rocket name must not be empty");
				}

				@ParameterizedTest
				@DisplayName("Should change rocket status")
				@EnumSource(value = RocketStatus.class, names = "IN_REPAIR", mode = EnumSource.Mode.EXCLUDE)
				void shouldChangeRocketStatus(RocketStatus newStatus) {

						//given
						var rocketId = "rocket1";
						var rocketName = "testRocket";
						var rocket = new Rocket(rocketId, rocketName);
						rocket.setStatus(null);
						rocketMap.put(rocketId, rocket);

						//when
						tested.setRocketStatus(rocketId, newStatus);

						//then
						assertThat(rocketMap.get(rocketId).getStatus()).isEqualTo(newStatus);
				}

				@Test
				void shouldChangeMissionStatusToPendingWhenRocketIsInRepair() {
						//given
						var rocketId = "rocket1";
						var missionId = "mission1";

						var rocketName = "testRocket";
						var missionName = "testMission";

						var rocket = new Rocket(rocketId, rocketName);
						rocket.setMissionId(missionId);
						rocketMap.put(rocketId, rocket);

						var mission = new Mission(missionId, missionName);
						mission.addRocket(rocketId);
						missionMap.put(missionId, mission);

						//when
						tested.setRocketStatus(rocketId, RocketStatus.IN_REPAIR);

						//then
						var storedMission = missionMap.get(missionId);
						assertThat(storedMission.getStatus()).isEqualTo(MissionStatus.PENDING);
				}
		}

		@Nested
		@DisplayName("Mission Management Tests")
		class MissionManagementTests {
				@Test
				@DisplayName("Should create and store new Mission")
				void shouldCreateAndStoreNewMission() {

						//given
						var missionName = "testMission";

						//when
						var createdMissionId = tested.addMission(missionName);

						//then
						var storedMission = missionMap.get(createdMissionId);
						assertThat(storedMission).isNotNull();
						assertThat(storedMission.getId()).isNotNull();
						assertThat(storedMission.getId()).isEqualTo(createdMissionId);
						assertThat(storedMission.getName()).isEqualTo(missionName);
						assertThat(storedMission.getStatus()).isEqualTo(MissionStatus.SCHEDULED);
						assertThat(storedMission.getRocketIds()).isNotNull();
						assertThat(storedMission.getRocketIds()).isEmpty();
				}

				@ParameterizedTest
				@NullAndEmptySource
				@DisplayName("Should throw exception on invalid Mission name")
				void shouldThrowExceptionOnInvalidMissionName(String missionName) {
						//when
						var exception = catchException(() -> tested.addMission(missionName));

						//then
						assertThat(exception).isInstanceOf(IllegalArgumentException.class);
						assertThat(exception.getMessage()).isEqualTo("Mission name must not be empty");
				}


				@ParameterizedTest
				@DisplayName("Should change Mission Status")
				@MethodSource
				void shouldChangeMissionStatus(MissionStatus newStatus, Set<Rocket> rockets) {

						//given
						var missionId = "mission1";
						var missionName = "testMission";
						var mission = new Mission(missionId, missionName);
						mission.setStatus(null);
						rockets.forEach(rocket -> {
								rocketMap.put(rocket.getId(), rocket);
								mission.addRocket(rocket.getId());
						});
						missionMap.put(missionId, mission);

						//when
						tested.setMissionStatus(missionId, newStatus);

						//then
						var storedMission = missionMap.get(missionId);
						assertThat(storedMission.getStatus()).isEqualTo(newStatus);
				}


				public static Stream<Arguments> shouldChangeMissionStatus() {
						var inRepariedRocket = new Rocket("rocket1", "inRepairRocket");
						inRepariedRocket.setStatus(RocketStatus.IN_REPAIR);

						var onGroundRocket = new Rocket("rocket2", "onGroundRocket");
						onGroundRocket.setStatus(RocketStatus.ON_GROUND);

						var inSpaceRocket = new Rocket("rocket3", "inSpaceRocket");
						inSpaceRocket.setStatus(RocketStatus.IN_SPACE);

						return Stream.of(
							Arguments.of(MissionStatus.SCHEDULED, Set.of()),
							Arguments.of(MissionStatus.ENDED, Set.of()),
							Arguments.of(MissionStatus.PENDING, Set.of(inRepariedRocket, onGroundRocket)),
							Arguments.of(MissionStatus.IN_PROGRESS, Set.of(inSpaceRocket, onGroundRocket))
						);
				}

				@Test
				@DisplayName("Should throw exception when changing Mission status form PENDING and there are Rockets in repair")
				void shouldThrowExceptionWhenChangingFormPending() {

						//given
						var rocketId = "rocket1";
						var rocketName = "testRocket1";
						var rocket1 = new Rocket(rocketId, rocketName);
						rocket1.setStatus(RocketStatus.IN_REPAIR);
						rocketMap.put(rocketId, rocket1);


						var missionId = "mission1";
						var missionName = "testMission";
						var mission = new Mission(missionId, missionName);
						mission.addRocket(rocketId);
						missionMap.put(missionId, mission);

						//when
						var exception = catchException(() -> tested.setMissionStatus(missionId, MissionStatus.IN_PROGRESS));

						//then
						assertThat(exception).isInstanceOf(IllegalStateException.class);
						assertThat(exception.getMessage()).isEqualTo("In progress Mission can't have rockets wit IN_REPAIR status");
				}

				@Test
				@DisplayName("Should throw exception when changing Mission status to IN_PROGRESS and there are no Rockets assigned")
				void shouldThrowExceptionWhenChangingFormToInProgressAndThereAreNoRocketAssigned() {

						var missionId = "mission1";
						var missionName = "testMission";
						var mission = new Mission(missionId, missionName);
						missionMap.put(missionId, mission);

						//when
						var exception = catchException(() -> tested.setMissionStatus(missionId, MissionStatus.IN_PROGRESS));

						//then
						assertThat(exception).isInstanceOf(IllegalStateException.class);
						assertThat(exception.getMessage()).isEqualTo("Mission does not have any Rockets assigned");
				}

				@Test
				@DisplayName("Should throw exception when changing status to ENDED and there are Rockets assigned")
				void shouldThrowExceptionWhenChangingStatusToEndedWithRocketsAssigned() {
						//given
						var rocketId = "rocket1";
						var rocketName = "testRocket1";
						var rocket1 = new Rocket(rocketId, rocketName);
						rocketMap.put(rocketId, rocket1);

						var missionId = "mission1";
						var missionName = "testMission";
						var mission = new Mission(missionId, missionName);
						mission.addRocket(rocketId);
						missionMap.put(missionId, mission);

						//when
						var exception = catchException(() -> tested.setMissionStatus(missionId, MissionStatus.ENDED));

						//then
						assertThat(exception).isInstanceOf(IllegalStateException.class);
						assertThat(exception.getMessage()).isEqualTo("Mission still has assigned rockets");
				}
		}

		@Nested
		@DisplayName("Rocket Assignment Tests")
		class RocketAssignmentTests {

				@Test
				@DisplayName("Should assign Rocket to Mission")
				void shouldAssignRocketToMission() {

						//given
						var rocketId = "rocket1";
						var rocketName = "testRocket";
						var rocket = new Rocket(rocketId, rocketName);
						rocketMap.put(rocketId, rocket);

						var missionId = "mission1";
						var missionName = "testMission";
						var mission = new Mission(missionId, missionName);
						missionMap.put(missionId, mission);

						//when
						tested.assignRocketToMission(rocketId, missionId);

						var storedRocket = rocketMap.get(rocketId);
						assertThat(storedRocket.getStatus()).isEqualTo(RocketStatus.IN_SPACE);

				}

				@Test
				@DisplayName("Should throw exception when assigning rockets to ended mission")
				void shouldThrowExceptionWhenAssigningRocketsToEndedMission() {
						//given
						var rocketId = "rocket1";
						var rocketName = "testRocket";
						var rocket = new Rocket(rocketId, rocketName);
						rocketMap.put(rocketId, rocket);

						var missionId = "mission1";
						var missionName = "testMission";
						var mission = new Mission(missionId, missionName);
						mission.setStatus(MissionStatus.ENDED);
						missionMap.put(missionId, mission);

						//when
						var exception = catchException(() -> tested.assignRocketToMission(rocketId, missionId));

						//then
						assertThat(exception).isInstanceOf(IllegalStateException.class);
						assertThat(exception.getMessage()).isEqualTo("Mission is already ended");
				}

				@Test
				@DisplayName("Should assign multiple Rockets to Mission")
				void shouldAssignMultipleRocketsToMission() {

						//given
						var missionId = "mission1";
						var missionName = "testMission";
						var mission = new Mission(missionId, missionName);
						missionMap.put(missionId, mission);

						var rocket1Id = "rocket1";
						var rocket1Name = "testRocket1";
						var rocket1 = new Rocket(rocket1Id, rocket1Name);
						rocketMap.put(rocket1Id, rocket1);

						var rocket2Id = "rocket2";
						var rocket2Name = "testRocket2";
						var rocket2 = new Rocket(rocket2Id, rocket2Name);
						rocketMap.put(rocket2Id, rocket2);

						//when
						tested.assignRocketToMission(rocket1Id, missionId);
						tested.assignRocketToMission(rocket2Id, missionId);

						//then
						var storedMission = missionMap.get(missionId);
						assertThat(storedMission.getRocketIds()).containsAll(Set.of(rocket1Id, rocket2Id));
				}

				@Test
				@DisplayName("Should throw exception when trying to assign non existing rocket")
				void shouldThrowExceptionWhenAssigningNonExistingRocket() {

						//given
						var nonExistingRocketId = "ghostRocket";

						var missionId = "mission1";
						var missionName = "testMission";
						var mission = new Mission(missionId, missionName);
						missionMap.put(missionId, mission);

						//when
						var exception = catchException(() -> tested.assignRocketToMission(nonExistingRocketId, missionId));

						//then
						assertThat(exception).isInstanceOf(IllegalArgumentException.class);
						assertThat(exception.getMessage()).isEqualTo("Rocket with id [%s] does not exists".formatted(nonExistingRocketId));
				}
		}

		@Nested
		@DisplayName("Mission Summary Tests")
		class MissionSummaryTests {

				@Test
				@DisplayName("Should return missions summary ordered by rocket count desc, then name desc")
				void shouldReturnMissionsSummaryOrderedByRocketCountDescThenNameDesc() {
						//given
						
						//missions
						tested.addMission("Mars");
						var luna1MissionId = tested.addMission("Luna1");
						var doubleLandingMissionId = tested.addMission("Double Landing");
						var transitMissionId = tested.addMission("Transit");
						tested.addMission("Luna2");
						var verticalLandingMissionId = tested.addMission("Vertical Landing");

						//dragons
						var dragon1Id = tested.addRocket("Dragon 1");
						var dragon2Id = tested.addRocket("Dragon 2");
						var redDragonId = tested.addRocket("Red Dragon");
						var dragonXLId = tested.addRocket("Dragon XL");
						var falconHeavyId = tested.addRocket("Falcon Heavy");

						//Set up Transit mission (3 rockets, IN_PROGRESS)
						tested.assignRocketToMission(redDragonId, transitMissionId);
						tested.assignRocketToMission(dragonXLId, transitMissionId);
						tested.assignRocketToMission(falconHeavyId, transitMissionId);
						tested.setRocketStatus(dragonXLId, RocketStatus.IN_SPACE);
						tested.setRocketStatus(falconHeavyId, RocketStatus.IN_SPACE);
						tested.setMissionStatus(transitMissionId, MissionStatus.IN_PROGRESS);

						//Set up Luna1 mission (2 rockets, PENDING)
						tested.assignRocketToMission(dragon1Id, luna1MissionId);
						tested.assignRocketToMission(dragon2Id, luna1MissionId);
						tested.setRocketStatus(dragon1Id, RocketStatus.IN_SPACE);
						tested.setRocketStatus(dragon2Id, RocketStatus.IN_REPAIR);

						//Set up ended missions
						tested.setMissionStatus(verticalLandingMissionId, MissionStatus.ENDED);
						tested.setMissionStatus(doubleLandingMissionId, MissionStatus.ENDED);

						//when
						var summary = tested.getMissionsSummary();

						//then
						assertThat(summary).hasSize(6);

						//Transit – In progress – Dragons: 3
						assertThat(summary.get(0).getMissionName()).isEqualTo("Transit");
						assertThat(summary.get(0).getMissionStatus()).isEqualTo(MissionStatus.IN_PROGRESS);
						assertThat(summary.get(0).getRockets().size()).isEqualTo(3);

						//Luna1 – Pending – Dragons: 2
						assertThat(summary.get(1).getMissionName()).isEqualTo("Luna1");
						assertThat(summary.get(1).getMissionStatus()).isEqualTo(MissionStatus.PENDING);
						assertThat(summary.get(1).getRockets().size()).isEqualTo(2);

						//Missions with 0 rockets should be ordered by name descending
						//Vertical Landing – Ended – Dragons: 0
						assertThat(summary.get(2).getMissionName()).isEqualTo("Vertical Landing");
						// Mars – Scheduled – Dragons: 0
						assertThat(summary.get(3).getMissionName()).isEqualTo("Mars");
						// Luna2 – Scheduled – Dragons: 0
						assertThat(summary.get(4).getMissionName()).isEqualTo("Luna2");
						// Double Landing – Ended – Dragons: 0
						assertThat(summary.get(5).getMissionName()).isEqualTo("Double Landing");
						
				}

				@Test
				@DisplayName("Should include rocket details in mission summary")
				void shouldIncludeRocketDetailsInMissionSummary() {
						// Given
						var missionId = tested.addMission("Transit");
						var rocket1Id = tested.addRocket("Red Dragon");
						var rocket2Id = tested.addRocket("Dragon XL");

						tested.assignRocketToMission(rocket1Id, missionId);
						tested.assignRocketToMission(rocket2Id, missionId);
						tested.setRocketStatus(rocket2Id, RocketStatus.IN_REPAIR);

						// When
						var summary = tested.getMissionsSummary();

						// Then
						assertThat(summary).hasSize(1);
						var missionSummary = summary.get(0);
						assertThat(missionSummary.getRockets())
							.extracting("rocketName", "rocketStatus")
							.containsExactlyInAnyOrder(
								tuple("Red Dragon", RocketStatus.IN_SPACE),
								tuple("Dragon XL", RocketStatus.IN_REPAIR)
							);
				}

				@Test
				@DisplayName("Should handle empty repository")
				void shouldHandleEmptyRepository() {
						// When
						var summary = tested.getMissionsSummary();

						// Then
						assertThat(summary).isEmpty();
				}
		}
		
}
