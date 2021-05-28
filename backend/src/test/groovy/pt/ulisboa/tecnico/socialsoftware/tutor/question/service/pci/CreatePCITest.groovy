package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ConnectionItemDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ConnectionLinkDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemConnectionQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto

@DataJpaTest
class CreatePCITest extends SpockTest{
    def setup() {
        createExternalCourseAndExecution()
    }
    def "create a connection question with at two elements on each side and 1 connection"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new ItemConnectionQuestionDto())

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
        questionDto.getQuestionDetailsDto().setConnections(con_ids)

        def optionDto4 = new ConnectionItemDto()
        optionDto4.setContent(OPTION_1_CONTENT)

        def leftGroup = new ArrayList<ConnectionItemDto>()
        leftGroup.add(optionDto3)
        leftGroup.add(optionDto4)

        def rightGroup = new ArrayList<ConnectionItemDto>()
        rightGroup.add(optionDto1)
        rightGroup.add(optionDto2)

        questionDto.getQuestionDetailsDto().setOptions(leftGroup, rightGroup)

        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "the correct question is inside the repository"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getImage() == null
        result.getCourse().getName() == COURSE_1_NAME
        def question = result.getQuestionDetails()
        question.getOptions().size() == 2
        question.getOptions().get(0).size() == 2
        question.getOptions().get(1).size() == 2
        def resOption = result.getQuestionDetails().getOptions()
        for (int i = 0; i < resOption.size(); i++){
            for (int j = 0; j < resOption.get(0).size(); j++){
                def option = resOption.get(i).get(j)
                option.getContent() == OPTION_1_CONTENT
            }
        }

        question.getConnections().size() == 1
        question.getConnections().get(0).getOrigin() == 0
        question.getConnections().get(0).getDestiny() == 0

    }

    def "create a connection question with more than one connection"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new ItemConnectionQuestionDto())

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
        questionDto.getQuestionDetailsDto().setConnections(con_ids)

        def optionDto4 = new ConnectionItemDto()
        optionDto4.setContent(OPTION_1_CONTENT)
        con_ids = new ArrayList<ConnectionLinkDto>()
        link1 = new ConnectionLinkDto(1,1)
        con_ids.add(link1)
        questionDto.getQuestionDetailsDto().setConnections(con_ids)

        def leftGroup = new ArrayList<ConnectionItemDto>()
        leftGroup.add(optionDto3)
        leftGroup.add(optionDto4)

        def rightGroup = new ArrayList<ConnectionItemDto>()
        rightGroup.add(optionDto1)
        rightGroup.add(optionDto2)

        questionDto.getQuestionDetailsDto().setOptions(leftGroup, rightGroup)

        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "the correct question is inside the repository"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        def question = result.getQuestionDetails()
        question.getConnections().size() == 2
        question.getConnections().get(0).getOrigin() == 0
        question.getConnections().get(0).getDestiny() == 0

        question.getConnections().get(1).getOrigin() == 1
        question.getConnections().get(1).getDestiny() == 1

    }

    def "create a connection question with more than 1 connection on one element"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new ItemConnectionQuestionDto())

        and: 'four options'
        def optionDto1 = new ConnectionItemDto()
        optionDto1.setContent(OPTION_1_CONTENT)

        def optionDto2 = new ConnectionItemDto()
        optionDto2.setContent(OPTION_1_CONTENT)

        def optionDto3 = new ConnectionItemDto()
        optionDto3.setContent(OPTION_1_CONTENT)
        def con_ids = new ArrayList<ConnectionLinkDto>()
        def link1 = new ConnectionLinkDto(0,0)
        def link2 = new ConnectionLinkDto(0,1)
        con_ids.add(link1)
        con_ids.add(link2)
        questionDto.getQuestionDetailsDto().setConnections(con_ids)


        def leftGroup = new ArrayList<ConnectionItemDto>()
        leftGroup.add(optionDto3)

        def rightGroup = new ArrayList<ConnectionItemDto>()
        rightGroup.add(optionDto1)
        rightGroup.add(optionDto2)

        questionDto.getQuestionDetailsDto().setOptions(leftGroup, rightGroup)

        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)
        then: "the correct question is inside the repository"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        def connections = result.getQuestionDetails().getConnections()
        connections.size() == 2
        connections.get(0).getOrigin() == 0
        connections.get(0).getDestiny() == 0
        connections.get(1).getOrigin() ==0
        connections.get(1).getDestiny() == 1
    }

    def "cannot create a connection question without at least 2 elements in one group and 1 in the other"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new ItemConnectionQuestionDto())

        def optionDto1 = new ConnectionItemDto()
        optionDto1.setContent(OPTION_1_CONTENT)

        def optionDto3 = new ConnectionItemDto()
        optionDto3.setContent(OPTION_1_CONTENT)
        def con_ids = new ArrayList<ConnectionLinkDto>()
        def link1 = new ConnectionLinkDto(0,0)
        con_ids.add(link1)
        questionDto.getQuestionDetailsDto().setConnections(con_ids)

        def leftGroup = new ArrayList<ConnectionItemDto>()
        leftGroup.add(optionDto3)

        def rightGroup = new ArrayList<ConnectionItemDto>()
        rightGroup.add(optionDto1)

        questionDto.getQuestionDetailsDto().setOptions(leftGroup, rightGroup)

        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.NOT_ENOUGH_OPTIONS_IN_GROUPS
    }

    def "cannot create a connection question without at least 1 connection"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new ItemConnectionQuestionDto())

        def optionDto1 = new ConnectionItemDto()
        optionDto1.setContent(OPTION_1_CONTENT)

        def optionDto2 = new ConnectionItemDto()
        optionDto2.setContent(OPTION_1_CONTENT)

        def optionDto3 = new ConnectionItemDto()
        optionDto3.setContent(OPTION_1_CONTENT)

        def leftGroup = new ArrayList<ConnectionItemDto>()
        leftGroup.add(optionDto3)

        def rightGroup = new ArrayList<ConnectionItemDto>()
        rightGroup.add(optionDto1)
        rightGroup.add(optionDto2)

        questionDto.getQuestionDetailsDto().setOptions(leftGroup, rightGroup)

        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.NO_CONNECTIONS_GIVEN
    }

    def "cannot create a connection question with an invalid connection"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new ItemConnectionQuestionDto())

        def optionDto1 = new ConnectionItemDto()
        optionDto1.setContent(OPTION_1_CONTENT)

        def optionDto2 = new ConnectionItemDto()
        optionDto2.setContent(OPTION_1_CONTENT)

        def optionDto3 = new ConnectionItemDto()
        optionDto3.setContent(OPTION_1_CONTENT)
        def con_ids = new ArrayList<ConnectionLinkDto>()
        def link1 = new ConnectionLinkDto(0,2)
        con_ids.add(link1)
        questionDto.getQuestionDetailsDto().setConnections(con_ids)

        def leftGroup = new ArrayList<ConnectionItemDto>()
        leftGroup.add(optionDto3)

        def rightGroup = new ArrayList<ConnectionItemDto>()
        rightGroup.add(optionDto1)
        rightGroup.add(optionDto2)

        questionDto.getQuestionDetailsDto().setOptions(leftGroup, rightGroup)

        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.INVALID_CONNECTIONS_GIVEN
    }
    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
