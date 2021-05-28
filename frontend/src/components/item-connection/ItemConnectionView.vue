<template>
  <div>
    <h3>Group Left:</h3>
    <ul>
      <li
        :data-cy="`groupLeft`"
        v-for="(item, index) in questionDetails.groupLeft"
        v-bind:key="index"
      >
        Item {{ index }} - Content: -> {{ item['content'] }}
      </li>
    </ul>
    <h3>Group Right</h3>
    <ul>
      <li
        :data-cy="`groupRight`"
        v-for="(item, index) in questionDetails.groupRight"
        v-bind:key="index"
      >
        Item {{ index }} - Content: -> {{ item['content'] }}
      </li>
    </ul>
    <h3>Connections:</h3>
    <ul>
      <li
        :data-cy="`row`"
        v-for="(link, index) in questionDetails.connections"
        v-bind:key="index"
      >
        Connection {{ index }} - {{ link['origin'] }} -> {{ link['destiny'] }}
      </li>
    </ul>
  </div>
</template>
<script lang="ts">
import { Component, Vue, Prop } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Question from '@/models/management/Question';
import Image from '@/models/management/Image';
import ItemConnectionQuestionDetails from '@/models/management/questions/pci/ItemConnectionQuestionDetails';
import ItemConnectionAnswerDetails from '@/models/management/questions/pci/ItemConnectionAnswerDetails';

@Component
export default class ItemConnectionView extends Vue {
  @Prop() readonly questionDetails!: ItemConnectionQuestionDetails;
  @Prop() readonly answerDetails?: ItemConnectionAnswerDetails;
}
</script>
