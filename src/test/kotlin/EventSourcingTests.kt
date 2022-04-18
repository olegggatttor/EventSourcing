import exceptions.AccountAlreadyEnteredException
import exceptions.AccountDoesNotExistException
import exceptions.AccountExpiredException
import exceptions.AccountNotEnteredException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import services.EnterService
import services.ManagerService
import services.ReportService
import storage.EventStorage
import java.time.Duration
import java.time.LocalDate
import kotlin.test.assertEquals


class EventSourcingTests {
    private val validAccount = "valid_account"
    private val validAccountTwo = "valid_account_2"
    private val invalidAccount = "invalid_account"
    private var storage = EventStorage()
    private var enterService = EnterService(storage)
    private var managerService = ManagerService(storage)
    private var reportService = ReportService(storage)

    @BeforeEach
    fun before() {
        storage = EventStorage()
        enterService = EnterService(storage)
        managerService = ManagerService(storage)
        reportService = ReportService(storage)
    }

    @Test
    fun `manager can create account for user`() {
        managerService.createAccount(validAccount, Duration.ofDays(30))
        Assertions.assertEquals(
            managerService.getAccountInfo(validAccount).login,
            validAccount
        )
    }

    @Test
    fun `manager can extend account for user that exists`() {
        managerService.createAccount(validAccount, Duration.ofDays(30))
        val expirationDate = managerService.getAccountInfo(validAccount).expirationDate

        managerService.extendAccount(validAccount, Duration.ofDays(60))
        val newExpirationDate = managerService.getAccountInfo(validAccount).expirationDate

        Assertions.assertEquals(expirationDate + Duration.ofDays(60), newExpirationDate)

    }

    @Test
    fun `existing user can enter and exit`() {
        managerService.createAccount(validAccount, Duration.ofDays(30))

        assertThrows<AccountNotEnteredException> {
            enterService.exit(validAccount)
        }

        enterService.enter(validAccount)

        assertThrows<AccountAlreadyEnteredException> {
            enterService.enter(validAccount)
        }

        enterService.exit(validAccount)

        assertThrows<AccountDoesNotExistException> {
            enterService.enter(invalidAccount)
        }

        assertThrows<AccountNotEnteredException> {
            enterService.exit(invalidAccount)
        }
    }

    @Test
    fun `report service provides correct stat`() {
        managerService.createAccount(validAccount, Duration.ofDays(30))
        managerService.createAccount(validAccountTwo, Duration.ofDays(30))


        enterService.enter(validAccount)
        enterService.enter(validAccountTwo)

        Thread.sleep(1000)

        enterService.exit(validAccount)
        enterService.exit(validAccountTwo)

        val daily = reportService.getDailyVisitsStat()
        val duration = reportService.getTotalDuration()
        val freq = reportService.getAverageFrequency()

        Assertions.assertTrue { daily[LocalDate.now()] == 2L }
        Assertions.assertTrue { duration != Duration.ZERO }
        Assertions.assertTrue { freq == 2.0 }
    }
}