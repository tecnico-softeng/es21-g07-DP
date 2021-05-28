package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.service.pci

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ConnectionItemDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ConnectionLinkDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemConnectionQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto

@DataJpaTest
class ImportExportPciQuestionsTest extends SpockTest {
    def questionId

    def setup() {
        createExternalCourseAndExecution()
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
        optionDto2.setContent(OPTION_2_CONTENT)

        def optionDto3 = new ConnectionItemDto()
        optionDto3.setContent(OPTION_3_CONTENT)

        def optionDto4 = new ConnectionItemDto()
        optionDto4.setContent(OPTION_4_CONTENT)

        def leftGroup = new ArrayList<ConnectionItemDto>()
        leftGroup.add(optionDto1)
        leftGroup.add(optionDto2)

        def rightGroup = new ArrayList<ConnectionItemDto>()
        rightGroup.add(optionDto3)
        rightGroup.add(optionDto4)

        def con_ids = new ArrayList<ConnectionLinkDto>()
        def link1 = new ConnectionLinkDto(0,0)
        con_ids.add(link1)
        questionDto.getQuestionDetailsDto().setConnections(con_ids)

        questionDto.getQuestionDetailsDto().setOptions(leftGroup, rightGroup)

        questionId = questionService.createQuestion(externalCourse.getId(), questionDto).getId()
    }

    def 'export questions to xml and import it'() {
        given: 'a xml with questions'
        def questionsXml = questionService.exportQuestionsToXml()
        print questionsXml
        and: 'a clean database'
        questionService.removeQuestion(questionId)

        when:
        questionService.importQuestionsFromXml(questionsXml)

        then:
        questionRepository.findQuestions(externalCourse.getId()).size() == 1
        def questionResult = questionService.findQuestions(externalCourse.getId()).get(0)
        questionResult.getKey() == null
        questionResult.getTitle() == QUESTION_1_TITLE
        questionResult.getContent() == QUESTION_1_CONTENT
        questionResult.getStatus() == Question.Status.AVAILABLE.name()
        def question = questionResult.getQuestionDetailsDto()
        def groupLeft = question.getGroupLeft()
        def groupRight = question.getGroupRight()
        groupLeft.size() == 2
        groupRight.size() == 2
        for (int j = 0; j < groupLeft.size(); j++){
            def option = groupLeft.get(j)
            option.getContent() == OPTION_1_CONTENT
        }
        for (int j = 0; j < groupRight.size(); j++){
            def option = groupRight.get(j)
            option.getContent() == OPTION_1_CONTENT
        }

        question.getConnections().size() == 1
        question.getConnections().get(0).getOrigin() == 0
        question.getConnections().get(0).getDestiny() == 0
    }

    def 'export to latex'() {
        when:
        def questionsLatex = questionService.exportQuestionsToLatex()

        then:
        questionsLatex != null
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}

}
