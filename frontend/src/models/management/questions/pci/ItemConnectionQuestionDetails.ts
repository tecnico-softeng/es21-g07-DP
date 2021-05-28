import ConnectionItem from '@/models/management/questions/pci/ConnectionItem';
import ConnectionLink from '@/models/management/questions/pci/ConnectionLink';

import QuestionDetails from '@/models/management/questions/QuestionDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';

export default class ItemConnectionQuestionDetails extends QuestionDetails {
  groupRight: ConnectionItem[] = [new ConnectionItem(), new ConnectionItem()];
  groupLeft: ConnectionItem[] = [new ConnectionItem(), new ConnectionItem()];
  connections: ConnectionLink[] = []

  constructor(jsonObj?: ItemConnectionQuestionDetails) {
    super(QuestionTypes.ItemConnection);
    if (jsonObj) {
      this.groupRight = jsonObj.groupRight.map((item: ConnectionItem) => new ConnectionItem(item));
      this.groupLeft = jsonObj.groupLeft.map((item: ConnectionItem) => new ConnectionItem(item));
      this.connections = jsonObj.connections.map((link: ConnectionLink) => new ConnectionLink(link));

    }
  }

  setAsNew(): void {
   // this.options.forEach(option => {
    //  option.id = null;
    //});
  }
}
