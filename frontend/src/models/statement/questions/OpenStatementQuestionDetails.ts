import StatementQuestionDetails from '@/models/statement/questions/StatementQuestionDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';

export default class OpenStatementQuestionDetails extends StatementQuestionDetails {
  teacherAnswer: string = '';

  constructor(jsonObj?: OpenStatementQuestionDetails) {
    super(QuestionTypes.Open);
    if (jsonObj) {
      this.teacherAnswer = jsonObj.teacherAnswer || this.teacherAnswer;
    }
  }
}
