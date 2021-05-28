import StatementCorrectAnswerDetails from '@/models/statement/questions/StatementCorrectAnswerDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';

export default class ItemConnectionStatementCorrectAnswerDetails extends StatementCorrectAnswerDetails {
  public correctOptionId: number | null = null;

  constructor(jsonObj?: ItemConnectionStatementCorrectAnswerDetails) {
    super(QuestionTypes.ItemConnection);
    //if (jsonObj) {
    //  this.correctOptionId = jsonObj.correctOptionId;
    //}
  }
}
