import StatementAnswerDetails from '@/models/statement/questions/StatementAnswerDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';
import ItemConnectionStatementCorrectAnswerDetails from '@/models/statement/questions/pci/ItemConnectionStatementCorrectAnswerDetails';

export default class ItemConnectionStatementAnswerDetails extends StatementAnswerDetails {
  public optionId: number | null = null;

  constructor(jsonObj?: ItemConnectionStatementAnswerDetails) {
    super(QuestionTypes.ItemConnection);
    //if (jsonObj) {
    //  this.optionId = jsonObj.optionId;
    //}
  }

  isQuestionAnswered(): boolean {
    return true;
    //return this.optionId != null;
  }

  isAnswerCorrect(
    correctAnswerDetails: ItemConnectionStatementCorrectAnswerDetails
  ): boolean {
    return true;// (
      //!!correctAnswerDetails &&
      //this.optionId === correctAnswerDetails.correctOptionId
   // );
  }
}
