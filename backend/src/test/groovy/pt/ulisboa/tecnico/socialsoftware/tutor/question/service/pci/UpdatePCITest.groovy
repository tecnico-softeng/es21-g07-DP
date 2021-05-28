package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemConnectionQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ConnectionItem
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ConnectionLink
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemConnectionQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ConnectionItemDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ConnectionLinkDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@DataJpaTest
class UpdatePCITest extends SpockTest {
    def question
    def option1
    def option2
    def option3
    def option4
    def leftGroup
    def rightGroup
    def connection1
    def connection2
    def connectionsList
    def user

    def setup() {

        createExternalCourseAndExecution()

        user = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        user.addCourse(externalCourseExecution)
        userRepository.save(user)
        
        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())

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

        question = questionService.createQuestion(externalCourse.getId(), questionDto)
        println("eu sou o print 1")
        println(question)

    }

    def "update a question, decrease to an invalid number of connections"() {
        given: "a changed question"
        println("eu sou o print 2")
        println(question)
        def newQuestionDto = new QuestionDto()
        newQuestionDto.setTitle(QUESTION_2_TITLE)
        newQuestionDto.setContent(QUESTION_2_CONTENT)
        newQuestionDto.setQuestionDetailsDto(new ItemConnectionQuestionDto())

        and: "unchanged options"
        option1 = new ConnectionItemDto()
        option1.setContent(OPTION_1_CONTENT)

        option2 = new ConnectionItemDto()
        option2.setContent(OPTION_2_CONTENT)

        option3 = new ConnectionItemDto()
        option3.setContent(OPTION_3_CONTENT)

        option4 = new ConnectionItemDto()
        option4.setContent(OPTION_4_CONTENT)

        and: "unchanged groups"
        leftGroup = new ArrayList<ConnectionItemDto>()
        leftGroup.add(option1)
        leftGroup.add(option2)

        rightGroup = new ArrayList<ConnectionItemDto>()
        rightGroup.add(option3)
        rightGroup.add(option4)

        newQuestionDto.getQuestionDetailsDto().setOptions(leftGroup, rightGroup)

        and: '2 changed connections'
        connectionsList = new ArrayList<ConnectionLinkDto>()

        newQuestionDto.getQuestionDetailsDto().setConnections(connectionsList)

        when:
        questionService.updateQuestion(question.getId(), newQuestionDto)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.NO_CONNECTIONS_GIVEN
    }

    def "update a question, decrease to a valid number of connections"() {
        given: "a changed question"
        def newQuestionDto = new QuestionDto()
        newQuestionDto.setTitle(QUESTION_2_TITLE)
        newQuestionDto.setContent(QUESTION_2_CONTENT)
        newQuestionDto.setQuestionDetailsDto(new ItemConnectionQuestionDto())

        and: "unchanged options"
        option1 = new ConnectionItemDto()
        option1.setContent(OPTION_1_CONTENT)

        option2 = new ConnectionItemDto()
        option2.setContent(OPTION_2_CONTENT)

        option3 = new ConnectionItemDto()
        option3.setContent(OPTION_3_CONTENT)

        option4 = new ConnectionItemDto()
        option4.setContent(OPTION_4_CONTENT)

        and: "unchanged groups"
        leftGroup = new ArrayList<ConnectionItemDto>()
        leftGroup.add(option1)
        leftGroup.add(option2)

        rightGroup = new ArrayList<ConnectionItemDto>()
        rightGroup.add(option3)
        rightGroup.add(option4)

        newQuestionDto.getQuestionDetailsDto().setOptions(leftGroup, rightGroup)

        and: "connections list"
        connectionsList = new ArrayList<ConnectionLinkDto>()

        and: 'one connection'
        connection1 = new ConnectionLinkDto(0, 0)
        connectionsList.add(connection1)

        newQuestionDto.getQuestionDetailsDto().setConnections(connectionsList)

        when:
        def r = questionService.updateQuestion(question.getId(), newQuestionDto)

        then: "the question is changed"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() == question.getId()
        result.getTitle() == QUESTION_2_TITLE
        result.getContent() == QUESTION_2_CONTENT
        and: 'a connection is changed'
        result.getQuestionDetails().getConnections().size() == 1
    }

    def "update a question, decrease to an invalid number of options"() {
        given: "a changed question"
        def newQuestionDto = new QuestionDto()
        newQuestionDto.setTitle(QUESTION_2_TITLE)
        newQuestionDto.setContent(QUESTION_2_CONTENT)
        newQuestionDto.setQuestionDetailsDto(new ItemConnectionQuestionDto())

        and: "2 changed option"
        option1 = new ConnectionItemDto()
        option1.setContent(OPTION_1_CONTENT)

        option2 = new ConnectionItemDto()
        option2.setContent(OPTION_2_CONTENT)


        and: "1 changed group"
        leftGroup = new ArrayList<ConnectionItemDto>()
        leftGroup.add(option1)
        leftGroup.add(option2)

        rightGroup = new ArrayList<ConnectionItemDto>()
        
        newQuestionDto.getQuestionDetailsDto().setOptions(leftGroup, rightGroup)

        when:
        questionService.updateQuestion(question.getId(), newQuestionDto)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.NOT_ENOUGH_OPTIONS_IN_GROUPS
    }

    def "update a question, decrease to a valid number of options"() {
        given: "a changed question"
        def newQuestionDto = new QuestionDto()
        newQuestionDto.setTitle(QUESTION_2_TITLE)
        newQuestionDto.setContent(QUESTION_2_CONTENT)
        newQuestionDto.setQuestionDetailsDto(new ItemConnectionQuestionDto())

        and: "changed options"

        option1 = new ConnectionItemDto()
        option1.setContent(OPTION_1_CONTENT)

        option2 = new ConnectionItemDto()
        option2.setContent(OPTION_2_CONTENT)

        option3 = new ConnectionItemDto()
        option3.setContent(OPTION_3_CONTENT)

        and: "changed groups"
        leftGroup = new ArrayList<ConnectionItemDto>()
        leftGroup.add(option1)
        leftGroup.add(option2)

        rightGroup = new ArrayList<ConnectionItemDto>()
        rightGroup.add(option3)

        newQuestionDto.getQuestionDetailsDto().setOptions(leftGroup, rightGroup)

        and: "connections list"
        connectionsList = new ArrayList<ConnectionLinkDto>()

        and: 'two connections'
        connection1 = new ConnectionLinkDto(0,0)
        connectionsList.add(connection1)

        connection2 = new ConnectionLinkDto(1,0)
        connectionsList.add(connection2)
        
        newQuestionDto.getQuestionDetailsDto().setConnections(connectionsList)

        when:
        questionService.updateQuestion(question.getId(), newQuestionDto)

        then: "the question is changed"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() == question.getId()
        result.getTitle() == QUESTION_2_TITLE
        result.getContent() == QUESTION_2_CONTENT
        and: 'a group is changed'
        result.getQuestionDetails().getOptions().size() == 2
        result.getQuestionDetails().getOptions().get(0).size() == 2
        result.getQuestionDetails().getOptions().get(1).size() == 1
    }
    
    def "update a question, increase number of options"() {
        given: "a changed question"
        def newQuestionDto = new QuestionDto()
        newQuestionDto.setTitle(QUESTION_2_TITLE)
        newQuestionDto.setContent(QUESTION_2_CONTENT)
        newQuestionDto.setQuestionDetailsDto(new ItemConnectionQuestionDto())

        and: "1 added option"

        option1 = new ConnectionItemDto()
        option1.setContent(OPTION_1_CONTENT)

        option2 = new ConnectionItemDto()
        option2.setContent(OPTION_2_CONTENT)

        option3 = new ConnectionItemDto()
        option3.setContent(OPTION_3_CONTENT)

        option4 = new ConnectionItemDto()
        option4.setContent(OPTION_4_CONTENT)

        def option5 = new ConnectionItemDto()
        option5.setContent(OPTION_5_CONTENT)

        and: "changed groups"
        leftGroup = new ArrayList<ConnectionItemDto>()
        leftGroup.add(option1)
        leftGroup.add(option2)

        rightGroup = new ArrayList<ConnectionItemDto>()
        rightGroup.add(option3)
        rightGroup.add(option4)
        rightGroup.add(option5)

        newQuestionDto.getQuestionDetailsDto().setOptions(leftGroup, rightGroup)

        and: "connections list"
        connectionsList = new ArrayList<ConnectionLinkDto>()

        and: 'two connections'
        connection1 = new ConnectionLinkDto(0,0)
        connectionsList.add(connection1)

        connection2 = new ConnectionLinkDto(1,0)
        connectionsList.add(connection2)
        
        newQuestionDto.getQuestionDetailsDto().setConnections(connectionsList)

        when:
        questionService.updateQuestion(question.getId(), newQuestionDto)

        then: "the question is changed"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() == question.getId()
        result.getTitle() == QUESTION_2_TITLE
        result.getContent() == QUESTION_2_CONTENT
        and: 'a group is changed'
        result.getQuestionDetails().getOptions().size() == 2
        result.getQuestionDetails().getOptions().get(0).size() == 2
        result.getQuestionDetails().getOptions().get(1).size() == 3
    }

    def "update a question, increase number of connections"() {
        given: "a changed question"
        def newQuestionDto = new QuestionDto()
        newQuestionDto.setTitle(QUESTION_2_TITLE)
        newQuestionDto.setContent(QUESTION_2_CONTENT)
        newQuestionDto.setQuestionDetailsDto(new ItemConnectionQuestionDto())

        and: "unchanged options"
        option1 = new ConnectionItemDto()
        option1.setContent(OPTION_1_CONTENT)

        option2 = new ConnectionItemDto()
        option2.setContent(OPTION_2_CONTENT)

        option3 = new ConnectionItemDto()
        option3.setContent(OPTION_3_CONTENT)

        option4 = new ConnectionItemDto()
        option4.setContent(OPTION_4_CONTENT)

        and: "unchanged groups"
        leftGroup = new ArrayList<ConnectionItemDto>()
        leftGroup.add(option1)
        leftGroup.add(option2)

        rightGroup = new ArrayList<ConnectionItemDto>()
        rightGroup.add(option3)
        rightGroup.add(option4)

        newQuestionDto.getQuestionDetailsDto().setOptions(leftGroup, rightGroup)

        and: "connections list"
        connectionsList = new ArrayList<ConnectionLinkDto>()

        and: '1 added connection'
        connection1 = new ConnectionLinkDto(0,0)
        connectionsList.add(connection1)

        connection2 = new ConnectionLinkDto(1,0)
        connectionsList.add(connection2)

        def connection3 = new ConnectionLinkDto(0,1)
        connectionsList.add(connection3)

        newQuestionDto.getQuestionDetailsDto().setConnections(connectionsList)

        when:
        questionService.updateQuestion(question.getId(), newQuestionDto)

        then: "the question is changed"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() == question.getId()
        result.getTitle() == QUESTION_2_TITLE
        result.getContent() == QUESTION_2_CONTENT
        and: 'a connection is changed'
        result.getQuestionDetails().getConnections().size() == 3
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
