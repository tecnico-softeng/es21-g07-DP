package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.service.pem

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@DataJpaTest
class ImportExportPemQuestionsTest extends SpockTest {
    def questionId
    def teacher

    def setup() {
        createExternalCourseAndExecution()
        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        def image = new ImageDto()
        image.setUrl(IMAGE_1_URL)
        image.setWidth(20)
        questionDto.setImage(image)

        def optionDto = new OptionDto()
        optionDto.setSequence(0)
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setOrder(1)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setSequence(1)
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setOrder(2)
        options.add(optionDto)

        optionDto = new OptionDto()
        optionDto.setSequence(2)
        optionDto.setContent(OPTION_3_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setOrder(3)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        optionDto = new OptionDto()
        optionDto.setSequence(3)
        optionDto.setContent(OPTION_4_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setOrder(4)
        options.add(optionDto)

        questionId = questionService.createQuestion(externalCourse.getId(), questionDto).getId()
    }

    def 'export and import questions to xml'() {
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
        def imageResult = questionResult.getImage()
        imageResult.getWidth() == 20
        imageResult.getUrl() == IMAGE_1_URL
        questionResult.getQuestionDetailsDto().getOptions().size() == 4
        def optionOneResult = questionResult.getQuestionDetailsDto().getOptions().get(0)
        def optionTwoResult = questionResult.getQuestionDetailsDto().getOptions().get(1)
        def optionThreeResult = questionResult.getQuestionDetailsDto().getOptions().get(2)
        def optionFourResult = questionResult.getQuestionDetailsDto().getOptions().get(3)
        optionOneResult.getSequence() + optionTwoResult.getSequence() + optionThreeResult.getSequence() + optionFourResult.getSequence() == 6
        optionOneResult.getContent() == OPTION_1_CONTENT
        optionTwoResult.getContent() == OPTION_2_CONTENT
        optionThreeResult.getContent() == OPTION_3_CONTENT
        optionFourResult.getContent() == OPTION_4_CONTENT
        optionOneResult.isCorrect()
        optionTwoResult.isCorrect()
        optionThreeResult.isCorrect()
        optionFourResult.isCorrect()
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
