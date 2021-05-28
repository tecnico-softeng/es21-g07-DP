package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import com.fasterxml.jackson.databind.ObjectMapper
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
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
class UpdateQuestionWebServiceIT extends SpockTest{
    @LocalServerPort
    private int port
    def response
    def questionDto
    def question
    def teacher

    def setup() {
        restClient = new RESTClient("http://localhost:" + port)

        createExternalCourseAndExecution()

        teacher = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.EXTERNAL)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        teacher.addCourse(externalCourseExecution)
        externalCourseExecution.addUser(teacher)
        userRepository.save(teacher)
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
    }

    def "update a multiple choice question for course execution"() {
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

        and: "a new questionDto"
        def newQuestionDto = new QuestionDto()
        newQuestionDto.setTitle(QUESTION_2_TITLE)
        newQuestionDto.setContent(QUESTION_2_CONTENT)
        newQuestionDto.setStatus(Question.Status.AVAILABLE.name())
        newQuestionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        and: 'updated options'
        def newOptionDto = new OptionDto()
        newOptionDto.setContent(OPTION_1_CONTENT)
        newOptionDto.setCorrect(false)
        def newOptions = new ArrayList<OptionDto>()
        newOptions.add(newOptionDto)
        newOptionDto = new OptionDto()
        newOptionDto.setContent(OPTION_2_CONTENT)
        newOptionDto.setCorrect(true)
        newOptionDto.setOrder(3)
        newOptions.add(newOptionDto)
        newOptionDto = new OptionDto()
        newOptionDto.setContent(OPTION_3_CONTENT)
        newOptionDto.setCorrect(true)
        newOptionDto.setOrder(1)
        newOptions.add(newOptionDto)
        newOptionDto = new OptionDto()
        newOptionDto.setContent(OPTION_4_CONTENT)
        newOptionDto.setCorrect(true)
        newOptionDto.setOrder(2)
        newOptions.add(newOptionDto)

        newQuestionDto.getQuestionDetailsDto().setOptions(newOptions)

        def om = new ObjectMapper()

        when:
        response = restClient.put(
                path: '/questions/' + question.getId(),
                body: om.writeValueAsString(newQuestionDto),
                requestContentType: 'application/json'
        )

        then: "check response status"
        response != null
        response.status == 200
        and: "if it responds with the updated open question"
        def questionData = response.data
        questionData != null
        questionData.id != null
        questionData.title == newQuestionDto.getTitle()
        questionData.content == newQuestionDto.getContent()
        questionData.status == newQuestionDto.getStatus()

        def responseOptions = questionData.questionDetailsDto.options
        responseOptions.get(0).content == OPTION_1_CONTENT
        responseOptions.get(0).correct == false

        responseOptions.get(1).content == OPTION_2_CONTENT
        responseOptions.get(1).correct == true
        responseOptions.get(1).order == 3

        responseOptions.get(2).content == OPTION_3_CONTENT
        responseOptions.get(2).correct == true
        responseOptions.get(2).order == 1

        responseOptions.get(3).content == OPTION_4_CONTENT
        responseOptions.get(3).correct == true
        responseOptions.get(3).order == 2

        cleanup:
        questionRepository.deleteById(question.getId())
    }

    def "update item connection question for course execution"() {
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


        and: "a new questionDto"
        def newQuestionDto = new QuestionDto()
        newQuestionDto.setKey(1)
        newQuestionDto.setTitle(QUESTION_2_TITLE)
        newQuestionDto.setContent(QUESTION_2_CONTENT)
        newQuestionDto.setStatus(Question.Status.AVAILABLE.name())
        newQuestionDto.setQuestionDetailsDto(new ItemConnectionQuestionDto())

        and: 'unchanged options'
        def newOptionDto1 = new ConnectionItemDto()
        newOptionDto1.setContent(OPTION_1_CONTENT)

        def newOptionDto2 = new ConnectionItemDto()
        newOptionDto2.setContent(OPTION_2_CONTENT)

        def newOptionDto3 = new ConnectionItemDto()
        newOptionDto3.setContent(OPTION_3_CONTENT)

        def newOptionDto4 = new ConnectionItemDto()
        newOptionDto4.setContent(OPTION_4_CONTENT)

        and: "left group and right group"
        def newLeftGroupDto = new ArrayList<ConnectionItemDto>()
        newLeftGroupDto.add(newOptionDto1)
        newLeftGroupDto.add(newOptionDto2)

        def newRightGroupDto = new ArrayList<ConnectionItemDto>()
        newRightGroupDto.add(newOptionDto3)
        newRightGroupDto.add(newOptionDto4)

        newQuestionDto.getQuestionDetailsDto().setOptions(newLeftGroupDto, newRightGroupDto)

        and: "connections list"
        def newConnectionsListDto = new ArrayList<ConnectionLinkDto>()

        and: 'one connection only'
        def newConnection1Dto = new ConnectionLinkDto(0, 0)
        newConnectionsListDto.add(newConnection1Dto)

        newQuestionDto.getQuestionDetailsDto().setConnections(newConnectionsListDto)


        def ow = new ObjectMapper().writer().withDefaultPrettyPrinter()

        when:
        response = restClient.put(
            path: '/questions/' + question.getId(),
            body: ow.writeValueAsString(newQuestionDto),
            requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == 200

        and: "if it responds with the correct item connection question"
        def questionResponse = response.data
        def itemConnectionDto = questionResponse.questionDetailsDto
        questionResponse != null
        itemConnectionDto.type == "item_connection"

        and: "the question is changed"
        questionResponse.id == question.getId()
        questionResponse.title == QUESTION_2_TITLE
        questionResponse.content == QUESTION_2_CONTENT
        questionResponse.status == newQuestionDto.getStatus()

        and: 'a connection is changed'
        itemConnectionDto.connections.size() == 1

        cleanup:
        questionRepository.deleteById(question.getId())
    }

    def "update an open question for course execution"() {
        given: "an OpenQuestionDto"
        questionDto.setQuestionDetailsDto(new OpenQuestionDto())
        questionDto.getQuestionDetailsDto().setTeacherAnswer(OPEN_QUESTION_TEACHER_ANSWER_1)

        and: "a question"
        question = new Question(externalCourse, questionDto)
        questionRepository.save(question)

        and: "a new questionDto"
        def newQuestionDto = new QuestionDto(question)
        newQuestionDto.setTitle(QUESTION_2_TITLE)
        newQuestionDto.setContent(QUESTION_2_CONTENT)
        newQuestionDto.setQuestionDetailsDto(new OpenQuestionDto())

        and: 'updated teacher answer'
        newQuestionDto.getQuestionDetailsDto().setTeacherAnswer(OPEN_QUESTION_TEACHER_ANSWER_2)
        def om = new ObjectMapper()

        when:
        response = restClient.put(
                path: '/questions/' + question.getId(),
                body: om.writeValueAsString(newQuestionDto),
                requestContentType: 'application/json'
        )

        then: "check response status"
        response != null
        response.status == 200
        and: "if it responds with the updated open question"
        def questionData = response.data
        questionData != null
        questionData.id != null
        questionData.title == newQuestionDto.getTitle()
        questionData.content == newQuestionDto.getContent()
        questionData.status == newQuestionDto.getStatus()
        questionData.questionDetailsDto.teacherAnswer == newQuestionDto.getQuestionDetailsDto().getTeacherAnswer()

        cleanup:
        questionRepository.deleteById(question.getId())
    }


    def "student cannot update question"() {
        given: "a student"
        def student = new User(USER_2_EMAIL, USER_2_EMAIL, USER_2_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.EXTERNAL)
        student.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        student.addCourse(externalCourseExecution)
        externalCourseExecution.addUser(student)
        userRepository.save(student)
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        and: "an OpenQuestionDto"
        questionDto.setQuestionDetailsDto(new OpenQuestionDto())
        questionDto.getQuestionDetailsDto().setTeacherAnswer(OPEN_QUESTION_TEACHER_ANSWER_1)

        and: "a question"
        question = new Question(externalCourse, questionDto)
        questionRepository.save(question)

        and: "a new questionDto"
        def newQuestionDto = new QuestionDto(question)
        newQuestionDto.setTitle(QUESTION_2_TITLE)
        newQuestionDto.setContent(QUESTION_2_CONTENT)
        newQuestionDto.setQuestionDetailsDto(new OpenQuestionDto())

        and: 'updated teacher answer'
        newQuestionDto.getQuestionDetailsDto().setTeacherAnswer(OPEN_QUESTION_TEACHER_ANSWER_2)
        def om = new ObjectMapper()

        when:
        response = restClient.put(
                path: '/questions/' + question.getId(),
                body: om.writeValueAsString(newQuestionDto),
                requestContentType: 'application/json'
        )

        then: "check response status"
        def error = thrown(HttpResponseException)
        error.response.status == 403

        cleanup:
        questionRepository.deleteById(question.getId())
        userRepository.deleteById(student.getId())
    }

    def "admin cannot update question"() {
        given: "an admin"
        demoAdminLogin()

        and: "an OpenQuestionDto"
        questionDto.setQuestionDetailsDto(new OpenQuestionDto())
        questionDto.getQuestionDetailsDto().setTeacherAnswer(OPEN_QUESTION_TEACHER_ANSWER_1)

        and: "a question"
        question = new Question(externalCourse, questionDto)
        questionRepository.save(question)

        and: "a new questionDto"
        def newQuestionDto = new QuestionDto(question)
        newQuestionDto.setTitle(QUESTION_2_TITLE)
        newQuestionDto.setContent(QUESTION_2_CONTENT)
        newQuestionDto.setQuestionDetailsDto(new OpenQuestionDto())

        and: 'updated teacher answer'
        newQuestionDto.getQuestionDetailsDto().setTeacherAnswer(OPEN_QUESTION_TEACHER_ANSWER_2)
        def om = new ObjectMapper()

        when:
        response = restClient.put(
                path: '/questions/' + question.getId(),
                body: om.writeValueAsString(newQuestionDto),
                requestContentType: 'application/json'
        )

        then: "check response status"
        def error = thrown(HttpResponseException)
        error.response.status == 403

        cleanup:
        questionRepository.deleteById(question.getId())
    }

    def "teacher cannot update question from another course"() {
        given: "a teacher"
        demoTeacherLogin()

        and: "an OpenQuestionDto"
        questionDto.setQuestionDetailsDto(new OpenQuestionDto())
        questionDto.getQuestionDetailsDto().setTeacherAnswer(OPEN_QUESTION_TEACHER_ANSWER_1)

        and: "a question"
        question = new Question(externalCourse, questionDto)
        questionRepository.save(question)

        and: "a new questionDto"
        def newQuestionDto = new QuestionDto(question)
        newQuestionDto.setTitle(QUESTION_2_TITLE)
        newQuestionDto.setContent(QUESTION_2_CONTENT)
        newQuestionDto.setQuestionDetailsDto(new OpenQuestionDto())

        and: 'updated teacher answer'
        newQuestionDto.getQuestionDetailsDto().setTeacherAnswer(OPEN_QUESTION_TEACHER_ANSWER_2)
        def om = new ObjectMapper()

        when:
        response = restClient.put(
                path: '/questions/' + question.getId(),
                body: om.writeValueAsString(newQuestionDto),
                requestContentType: 'application/json'
        )

        then: "check response status"
        def error = thrown(HttpResponseException)
        error.response.status == 403

        cleanup:
        questionRepository.deleteById(question.getId())
    }

    def cleanup() {
        userRepository.deleteById(teacher.getId())
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourse.getId())
    }
}
