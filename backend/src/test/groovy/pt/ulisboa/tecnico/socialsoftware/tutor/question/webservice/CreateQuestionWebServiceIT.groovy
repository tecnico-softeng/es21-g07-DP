package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import groovy.json.JsonOutput
import com.fasterxml.jackson.databind.ObjectMapper
import groovyx.net.http.RESTClient
import groovyx.net.http.HttpResponseException
import org.springframework.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OpenQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ConnectionItemDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ConnectionLinkDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemConnectionQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def teacher
    def course
    def courseExecution
    def response
    def questionDto

    def setup() {
        given: "a rest client"
        restClient = new RESTClient("http://localhost:" + port)

        createExternalCourseAndExecution()

        and: "a question"
        questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
    }

    def "create item connection question for course execution"() {
        given: "a questionDto"
        def itemConnectionQuestionDto = new ItemConnectionQuestionDto()
        questionDto.setQuestionDetailsDto(itemConnectionQuestionDto)

        and: "a teacher"
        demoTeacherLogin()

        and: 'four options'
        def optionDto1 = new ConnectionItemDto()
        optionDto1.setContent(OPTION_1_CONTENT)
        def optionDto2 = new ConnectionItemDto()
        optionDto2.setContent(OPTION_1_CONTENT)
        def optionDto3 = new ConnectionItemDto()
        optionDto3.setContent(OPTION_1_CONTENT)
        def con_ids = new ArrayList<ConnectionLinkDto>()
        def link1 = new ConnectionLinkDto(0,0)
        con_ids.add(link1)

        itemConnectionQuestionDto.setConnections(con_ids)
        def optionDto4 = new ConnectionItemDto()
        optionDto4.setContent(OPTION_1_CONTENT)
        def leftGroup = new ArrayList<ConnectionItemDto>()
        leftGroup.add(optionDto3)
        leftGroup.add(optionDto4)
        def rightGroup = new ArrayList<ConnectionItemDto>()
        rightGroup.add(optionDto1)
        rightGroup.add(optionDto2)

        itemConnectionQuestionDto.setOptions(leftGroup, rightGroup)

        def ow = new ObjectMapper().writer().withDefaultPrettyPrinter()
        when:
        response = restClient.post(
                path: '/courses/' +courseService.getDemoCourse().getCourseId() + '/questions',
                body: ow.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == 200
        and: "if it responds with the correct item connection question"
        def question = response.data
        def itemConnectionDto = question.questionDetailsDto
        question != null
        question.id != null
        question.title == questionDto.getTitle()
        question.content == questionDto.getContent()
        itemConnectionDto.type == "item_connection"
        for (int i = 0; i < itemConnectionDto.groupRight.size(); i++){
            itemConnectionDto.groupRight.get(i).content == OPTION_1_CONTENT
        }
        for (int i = 0; i < itemConnectionDto.groupLeft.size(); i++){
            itemConnectionDto.groupLeft.get(i).content == OPTION_1_CONTENT
        }
        itemConnectionDto.connections.get(0).origin == 0
        itemConnectionDto.connections.get(0).destiny == 0

    }

    def "create open question for course execution"() {
        given: "a teacher"
        demoTeacherLogin()

        and: "an openQuestionDto"
        def openQuestionDto = new OpenQuestionDto()
        openQuestionDto.setTeacherAnswer(OPEN_QUESTION_TEACHER_ANSWER_1)
        questionDto.setQuestionDetailsDto(openQuestionDto)

        def om = new ObjectMapper()

        when:
        response = restClient.post(
                path: '/courses/' + courseService.getDemoCourse().getCourseId() + '/questions',
                body: om.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == 200
        and: "if it responds with the correct open question"
        def question = response.data
        question != null
        question.id != null
        question.title == questionDto.getTitle()
        question.content == questionDto.getContent()
        question.status == questionDto.getStatus()
        question.questionDetailsDto.teacherAnswer == questionDto.getQuestionDetailsDto().getTeacherAnswer()
    }

    def "create a multiple choice question for course execution"() {
        given: "a teacher"
        demoTeacherLogin()

        and: "a multipleChoiceQuestionDto"
        def multipleChoiceQuestionDto = new MultipleChoiceQuestionDto()
        questionDto.setQuestionDetailsDto(multipleChoiceQuestionDto)

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

        def om = new ObjectMapper()

        when:
        response = restClient.post(
                path: '/courses/' + courseService.getDemoCourse().getCourseId() + '/questions',
                body: om.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == 200
        and: "if it responds with the correct open question"
        def question = response.data
        question != null
        question.id != null
        question.title == questionDto.getTitle()
        question.content == questionDto.getContent()
        question.status == questionDto.getStatus()

        def responseOptions = question.questionDetailsDto.options
        responseOptions.get(0).content == OPTION_1_CONTENT
        responseOptions.get(0).correct == true
        responseOptions.get(0).order == 1

        responseOptions.get(1).content == OPTION_2_CONTENT
        responseOptions.get(1).correct == false

        responseOptions.get(2).content == OPTION_3_CONTENT
        responseOptions.get(2).correct == true
        responseOptions.get(2).order == 2

        responseOptions.get(3).content == OPTION_4_CONTENT
        responseOptions.get(3).correct == true
        responseOptions.get(3).order == 3
    }

    def "student cannot create an available question"() {
        given: "a student"
        demoStudentLogin()

        and: "a multipleChoiceQuestionDto"
        def multipleChoiceQuestionDto = new MultipleChoiceQuestionDto()
        questionDto.setQuestionDetailsDto(multipleChoiceQuestionDto)

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

        def om = new ObjectMapper()

        when:
        response = restClient.post(
                path: '/courses/' + courseService.getDemoCourse().getCourseId() + '/questions',
                body: JsonOutput.toJson(questionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        def error = thrown(HttpResponseException)
        error.response.status == 403
    }

    def "teacher cannot create a question in a course that he is not in"() {
        given: "a teacher"
        demoTeacherLogin()

        and: "a multipleChoiceQuestionDto"
        def multipleChoiceQuestionDto = new MultipleChoiceQuestionDto()
        questionDto.setQuestionDetailsDto(multipleChoiceQuestionDto)

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

        def om = new ObjectMapper()

        when:
        response = restClient.post(
                path: '/courses/' + externalCourse.getId() + '/questions',
                body: JsonOutput.toJson(questionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        def error = thrown(HttpResponseException)
        error.response.status == 403
    }


    def "admin cannot create a question in a course that he is not in"() {
        given: "an admin"
        demoAdminLogin()

        and: "a multipleChoiceQuestionDto"
        def multipleChoiceQuestionDto = new MultipleChoiceQuestionDto()
        questionDto.setQuestionDetailsDto(multipleChoiceQuestionDto)

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

        def om = new ObjectMapper()

        when:
        response = restClient.post(
                path: '/courses/' + externalCourse.getId() + '/questions',
                body: JsonOutput.toJson(questionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        def error = thrown(HttpResponseException)
        error.response.status == 403
    }

    def cleanup() {
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourse.getId())
    }
}
