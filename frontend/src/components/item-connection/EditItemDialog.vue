<template>
  <v-dialog
    :value="true"
    @input="$emit('close', false)"
    @keydown.esc="$emit('close', false)"
    max-width="50%"
    max-height="50%"
  >
    <v-card style="padding-left: 2%; padding-right: 2%">
      <v-card-title> Edit Option </v-card-title>
      <v-textarea 
        :data-cy="`Edit`"
        rows="1" auto-grow v-model="content"
        >{{ this.content }}
      </v-textarea>
      <v-card-actions>
        <v-spacer />
        <v-btn
          color="red darken-1" 
          @click="$emit('close', false)"
          >Cancel
        </v-btn>
        <v-btn
          :data-cy="`Save`"
          color="green darken-1"
          @click="$emit('save', content)"
          >Save
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Model, Prop, Vue, Watch } from 'vue-property-decorator';

@Component
export default class EditItemDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: String }) content: string = '';
}
</script>
