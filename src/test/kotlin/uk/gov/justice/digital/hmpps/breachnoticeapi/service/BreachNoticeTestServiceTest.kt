package uk.gov.justice.digital.hmpps.breachnoticeapi.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.Address
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNotice
import uk.gov.justice.digital.hmpps.breachnoticeapi.model.BreachNoticeDetails
import uk.gov.justice.digital.hmpps.breachnoticeapi.repository.BreachNoticeRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@SpringBootTest
//@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class BreachNoticeTestServiceTest(@Autowired private val breachNoticeRepository: BreachNoticeRepository){
  private val breachNoticeService = BreachNoticeService(breachNoticeRepository)

  val basicaddress: Address = Address(
    addressNumber = "1",
    streetName = "OFFENDERSTREET",
    district = "OFFENDERDISTRICT",
    townCity = "OFFENDERTOWN",
    county = "OFFENDERCOUNTY",
    postcode = "NE25 9AB"
  )


  val breachNotice: BreachNotice = BreachNotice(
    crn = "X03489B",
    dateOfLetter = LocalDate.now(),
    referenceNumber = "ABC1234565",
    responseRequiredDate = LocalDate.now(),
    breachNoticeTypeCode = "TYPE0NOGATIVE",
    breachConditionTypeCode = "TYPE0NOGATIVE",
    responsibleOfficer = "RESPONSIBLEPETE",
    contactNumber = "01912525252",
    nextAppointmentType = "TEST",
    nextAppointmentDate = LocalDateTime.now(),
    nextAppointmentLocation = "TEST_LOCATION",
    nextAppointmentOfficer = "TEST_OFFICER",
//    nextAppointmentContactId = UUID.randomUUID(),
    completedDate = LocalDateTime.now(),
    offenderAddress = basicaddress,
    replyAddress = basicaddress
  )


  @Test
  fun `should successfully create a new BreachNotice`() {
    val breachNoticeUuid : UUID = breachNoticeService.createBreachNotice(breachNotice)
    val retrievedBreachNotice : BreachNoticeDetails? = breachNoticeService.getBreachNoticeById(breachNoticeUuid);
    assertThat(retrievedBreachNotice?.crn, equalTo("X03489B"));
  }
}