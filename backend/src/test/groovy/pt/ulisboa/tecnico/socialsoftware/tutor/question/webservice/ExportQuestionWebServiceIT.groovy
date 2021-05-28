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
class ExportQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def teacher
    def questionDto
    def question
    def student
  
    def setup() {
        restClient = new RESTClient("http://localhost:" + port)

        createExternalCourseAndExecution();

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

    def "export multiple choice question"() {
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

        and: "prepare request response"
        restClient.handler.failure = { resp, reader ->
            [response:resp, reader:reader]
        }
        restClient.handler.success = { resp, reader ->
            [response:resp, reader:reader]
        }

        when:
        def response_map = restClient.get(
                path: '/courses/' + externalCourse.getId() + '/questions/export',
                requestContentType: 'application/json'
        )

        def responseJson = response_map["response"]
        def readerJson =  response_map["reader"]

        then: "check the response status"
        responseJson != null
        responseJson.status == 200
        readerJson != null

        cleanup:
        questionRepository.deleteById(question.getId())
    }

    def "export item connection question"() {
        given: "a questionDto"
        questionDto.setQuestionDetailsDto(new ItemConnectionQuestionDto())

        and: 'four options'
        def optionDto1 = new ConnectionItemDto()
        optionDto1.setContent(OPTION_1_CONTENT)

        def optionDto2 = new ConnectionItemDto()
        optionDto2.setContent(OPTION_2_CONTENT)

        def optionDto3 = new ConnectionItemDto()
        optionDto3.setContent(OPTION_3_CONTENT)

        def con_ids = new ArrayList<ConnectionLinkDto>()


        def optionDto4 = new ConnectionItemDto()
        optionDto4.setContent(OPTION_4_CONTENT)

        def link1 = new ConnectionLinkDto(0,0)
        def link2 = new ConnectionLinkDto(1,0)
        con_ids.add(link1)
        con_ids.add(link2)
        questionDto.getQuestionDetailsDto().setConnections(con_ids)

        def leftGroup = new ArrayList<ConnectionItemDto>()
        leftGroup.add(optionDto3)
        leftGroup.add(optionDto4)

        def rightGroup = new ArrayList<ConnectionItemDto>()
        rightGroup.add(optionDto1)
        rightGroup.add(optionDto2)

        questionDto.getQuestionDetailsDto().setOptions(leftGroup, rightGroup)
        def question = questionService.createQuestion(externalCourse.getId(), questionDto)

        and: "prepare request response"
        restClient.handler.failure = { resp, reader ->
            [response:resp, reader:reader]
        }
        restClient.handler.success = { resp, reader ->
            [response:resp, reader:reader]
        }

        when:
        def response_map = restClient.get(
                path: '/courses/' + externalCourse.getId() + '/questions/export',
                requestContentType: 'application/json'
        )

        def responseJson = response_map["response"]
        def readerJson =  response_map["reader"]

        then: "check the response status"
        responseJson != null
        responseJson.status == 200
        readerJson != null

        cleanup:
        questionRepository.deleteById(question.getId())
    }

    def "export course questions"() {
        given: "an open question dto"
        questionDto.setQuestionDetailsDto(new OpenQuestionDto())
        questionDto.getQuestionDetailsDto().setTeacherAnswer(OPEN_QUESTION_TEACHER_ANSWER_1)
        question = new Question(externalCourse, questionDto)
        questionRepository.save(question)

        and: "prepare request response"
        restClient.handler.failure = { resp, reader ->
            [response:resp, reader:reader]
        }
        restClient.handler.success = { resp, reader ->
            [response:resp, reader:reader]
        }

        when: "the webservice is invoked"
        def response_map = restClient.get(
                path: '/courses/' + externalCourse.getId() + '/questions/export',
                requestContentType: 'application/json'
        )

        def responseJson = response_map["response"]
        def readerJson =  response_map["reader"]

        then: "check the response status"
        responseJson != null
        responseJson.status == 200
        readerJson != null

        cleanup:
        questionRepository.deleteById(question.getId())
    }

    def "teacher cannot export question from other teacher"() {
        given: "a admin"
        demoTeacherLogin()

        and: "an open question dto"
        questionDto.setQuestionDetailsDto(new OpenQuestionDto())
        questionDto.getQuestionDetailsDto().setTeacherAnswer(OPEN_QUESTION_TEACHER_ANSWER_1)

        question = new Question(externalCourse, questionDto)
        questionRepository.save(question)
      
        when: "the webservice is invoked"
        def response_map = restClient.get(
                path: '/courses/' + externalCourse.getId() + '/questions/export',
                requestContentType: 'application/json'
        )

        then: "check the response status"
        def error = thrown(HttpResponseException)
        error.response.status == 403
    }

    def "admin doesnt have export permission"() {
        given: "a admin"
        demoAdminLogin()

        and: "an open question dto"
        questionDto.setQuestionDetailsDto(new OpenQuestionDto())
        questionDto.getQuestionDetailsDto().setTeacherAnswer(OPEN_QUESTION_TEACHER_ANSWER_1)

        question = new Question(externalCourse, questionDto)
        questionRepository.save(question)

        when: "the webservice is invoked"
        def response_map = restClient.get(
                path: '/courses/' + externalCourse.getId() + '/questions/export',
                requestContentType: 'application/json'
        )

        then: "check the response status"
        def error = thrown(HttpResponseException)
        error.response.status == 403
    }
  
    def "student doesnt have export permission"() {
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

        when: "the webservice is invoked"
        def response_map = restClient.get(
                path: '/courses/' + externalCourse.getId() + '/questions/export',
                requestContentType: 'application/json'
        )

        then: "check the response status"
        def error = thrown(HttpResponseException)
        error.response.status == 403

        cleanup:
        userRepository.deleteById(student.getId())
    }


    def cleanup() {
        userRepository.deleteById(teacher.getId())
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourse.getId())
    }

}
