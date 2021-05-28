package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OpenQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ConnectionItemDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ConnectionLinkDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemConnectionQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RemoveQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def teacher
    def response
    def questionDto
    def question

    def student

    def setup() {
        restClient = new RESTClient("http://localhost:" + port)

        createExternalCourseAndExecution()

        teacher = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.EXTERNAL)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        teacher.addCourse(externalCourseExecution)
        externalCourseExecution.addUser(teacher)
        userRepository.save(teacher)

        questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())

        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)
    }

    def "remove multiple choice question"() {
        given: "create a question"
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setOrder(1)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(false)
        options.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setContent(OPTION_3_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setOrder(2)
        options.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setContent(OPTION_4_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setOrder(3)
        options.add(optionDto)

        questionDto.getQuestionDetailsDto().setOptions(options)

        def question = questionService.createQuestion(externalCourse.getId(), questionDto)

        when:
        response = restClient.delete(
            path: '/questions/' + question.getId()
        )

        then: "check the response status"
        response != null
        response.status == 200
        and: "if it responds with the correct item connection question"
        response.data == null
    }

    def "remove item connection question"() {
        given: "create a question"
        questionDto.setQuestionDetailsDto(new ItemConnectionQuestionDto())

        and: 'four options'
        def optionDto1 = new ConnectionItemDto()
        optionDto1.setContent(OPTION_1_CONTENT)

        def optionDto2 = new ConnectionItemDto()
        optionDto2.setContent(OPTION_2_CONTENT)

        def optionDto3 = new ConnectionItemDto()
        optionDto3.setContent(OPTION_3_CONTENT)

        def optionDto4 = new ConnectionItemDto()
        optionDto4.setContent(OPTION_4_CONTENT)

        and: "left group and right group"
        def leftGroupDto = new ArrayList<ConnectionItemDto>()
        leftGroupDto.add(optionDto1)
        leftGroupDto.add(optionDto2)

        def rightGroupDto = new ArrayList<ConnectionItemDto>()
        rightGroupDto.add(optionDto3)
        rightGroupDto.add(optionDto4)

        questionDto.getQuestionDetailsDto().setOptions(leftGroupDto, rightGroupDto)

        and: "connections list"
        def connectionsListDto = new ArrayList<ConnectionLinkDto>()

        and: 'two connections'
        def connection1Dto = new ConnectionLinkDto(0, 0)
        connectionsListDto.add(connection1Dto)
        def connection2Dto = new ConnectionLinkDto(0, 1)
        connectionsListDto.add(connection2Dto)


        questionDto.getQuestionDetailsDto().setConnections(connectionsListDto)

        def question = questionService.createQuestion(externalCourse.getId(), questionDto)

        when:
        response = restClient.delete(
            path: '/questions/' + question.getId()
        )

        then: "check the response status"
        response != null
        response.status == 200
        and: "if it responds with the correct item connection question"
        response.data == null

    }

    def "remove open question"() {
        given: "an open question dto"
        questionDto.setQuestionDetailsDto(new OpenQuestionDto())
        questionDto.getQuestionDetailsDto().setTeacherAnswer(OPEN_QUESTION_TEACHER_ANSWER_1)

        question = new Question(externalCourse, questionDto)
        questionRepository.save(question)

        when:
        response = restClient.delete(
                path: '/questions/' + question.getId(),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == 200
    }

    def "student doesnt have remove permission"() {
        given: "a student"
        student = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.EXTERNAL)
        student.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        student.addCourse(externalCourseExecution)
        userRepository.save(student)
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        and: "an open question dto"
        questionDto.setQuestionDetailsDto(new OpenQuestionDto())
        questionDto.getQuestionDetailsDto().setTeacherAnswer(OPEN_QUESTION_TEACHER_ANSWER_1)

        question = new Question(externalCourse, questionDto)
        questionRepository.save(question)

        when:
        response = restClient.delete(
                path: '/questions/' + question.getId(),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        def error = thrown(HttpResponseException)
        error.response.status == 403

        cleanup:
        userRepository.deleteById(student.getId())
    }

    def "admin doesnt have remove permission"() {
        given: "a admin"
        demoAdminLogin()

        and: "an open question dto"
        questionDto.setQuestionDetailsDto(new OpenQuestionDto())
        questionDto.getQuestionDetailsDto().setTeacherAnswer(OPEN_QUESTION_TEACHER_ANSWER_1)

        question = new Question(externalCourse, questionDto)
        questionRepository.save(question)

        when:
        response = restClient.delete(
                path: '/questions/' + question.getId(),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        def error = thrown(HttpResponseException)
        error.response.status == 403
    }

    def "teacher doesnt have remove permission for another course"() {
        given: "a teacher"
        demoTeacherLogin()

        and: "an open question dto"
        questionDto.setQuestionDetailsDto(new OpenQuestionDto())
        questionDto.getQuestionDetailsDto().setTeacherAnswer(OPEN_QUESTION_TEACHER_ANSWER_1)

        question = new Question(externalCourse, questionDto)
        questionRepository.save(question)

        when:
        response = restClient.delete(
                path: '/questions/' + question.getId(),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        def error = thrown(HttpResponseException)
        error.response.status == 403
    }

    def cleanup() {
        userRepository.deleteById(teacher.getId())
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourse.getId())
    }
}

