<template>
  <v-row style="position: relative">
    <canvas
      id="canvas"
      @mousemove="hover($event)"
      @click="removeConnections($event)"
    >
    </canvas>
    <v-col cols="4" offset="1">
      <h3 style="text-align: center">Group Left</h3>
    </v-col>

    <v-col cols="4" offset="2">
      <h3 style="text-align: center">Group Right</h3>
    </v-col>
    <v-col cols="4" offset="1">
      <v-row
        v-for="(option, index) in sQuestionDetails.groupLeft"
        :key="index"
        style="padding-bottom: 10px"
      >
        <v-col>
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                class="ma-1 action-button"
                v-on="on"
                x-small
                style="z-index: 1"
                @click="removeItem(0, index)"
                >close
              </v-icon>
            </template>
            <span>Remove Option</span>
          </v-tooltip>
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                class="ma-1 action-button"
                :data-cy="`Edit_Left_${index}`"
                v-on="on"
                x-small
                style="z-index: 1"
                @click="editItem(0, index)"
                >mdi-lead-pencil
              </v-icon>
            </template>
            <span>Edit Option</span>
          </v-tooltip>
        </v-col>
        <v-card class="item" style="z-index: 1">
          {{ sQuestionDetails.groupLeft[index].content }}
        </v-card>
        <v-btn
          :id="'left_' + index"
          class="mx-2"
          style="width: 20px; height: 20px; z-index: 1"
          :data-cy="`Click_Left_${index}`"
          fab
          dark
          x-small
          color="primary"
          @click="setLine($event, index, 0, true)"
        >
        </v-btn>
      </v-row>
      <v-row style="padding-bottom: 10px">
        <v-btn
          class="ma-auto"
          color="blue darken-1"
          @click="addItem(0)"
          data-cy="addItemLeft"
          style="z-index: 1"
          >Add Item
        </v-btn>
      </v-row>
    </v-col>

    <v-col cols="4" offset="2">
      <v-row
        v-for="(option, index) in sQuestionDetails.groupRight"
        :key="index"
        style="padding-bottom: 10px"
      >
        <v-btn
          :id="'right_' + index"
          class="mx-2"
          style="width: 20px; height: 20px; z-index: 1"
          :data-cy="`Click_Right_${index}`"
          fab
          dark
          x-small
          color="primary"
          @click="setLine($event, index, 1, true)"
        >
        </v-btn>
        <v-card class="item" style="z-index: 1">
          {{ sQuestionDetails.groupRight[index].content }}
        </v-card>
        <v-col>
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                class="ma-1 action-button"
                v-on="on"
                x-small
                style="z-index: 1"
                @click="removeItem(1, index)"
                >close
              </v-icon>
            </template>
            <span>Remove Option</span>
          </v-tooltip>
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                class="ma-1 action-button"
                :data-cy="`Edit_Right_${index}`"
                v-on="on"
                x-small
                style="z-index: 1"
                @click="editItem(1, index)"
                >mdi-lead-pencil
              </v-icon>
            </template>
            <span>Edit Option</span>
          </v-tooltip>
        </v-col>
      </v-row>
      <v-row style="padding-bottom: 10px">
        <v-btn
          class="ma-auto"
          color="blue darken-1"
          @click="addItem(1)"
          data-cy="addItemLeft"
          style="z-index: 1"
          >Add Item
        </v-btn>
      </v-row>
    </v-col>
    <edit-item-dialog
      v-if="editItemDialog"
      :content="editingContent"
      v-on:close="onClose"
      v-on:save="onSave"
    />
  </v-row>
</template>
<style>
.item {
  width: calc(100% - 100px);
}

#canvas {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: 0;
}

.v-btn {
  min-width: 0;
  min-height: 0;
}
</style>

<script lang="ts">
import { Component, Model, PropSync, Vue, Watch } from 'vue-property-decorator';
import ItemConnectionQuestionDetails from '@/models/management/questions/pci/ItemConnectionQuestionDetails';
import ConnectionItem from '@/models/management/questions/pci/ConnectionItem';
import ConnectionLink from '@/models/management/questions/pci/ConnectionLink';
import EditItemDialog from '@/components/item-connection/EditItemDialog.vue';

@Component({ components: { 'edit-item-dialog': EditItemDialog } })
export default class ItemConnectionCreate extends Vue {
  @PropSync('questionDetails', { type: ItemConnectionQuestionDetails })
  sQuestionDetails!: ItemConnectionQuestionDetails;
  flagSize: boolean = false;
  flagDrawing: number = 0;
  idx: number = -1;
  group: string = '';
  hoveredLines: number[] = [];
  editItemDialog: Boolean = false;
  editingIndex: number = -1;
  editingGroup: number = -1;
  editingContent: String | null = null;

  setLine(event: Event, index: number, group: number) {
    var c = document.getElementById('canvas') as HTMLCanvasElement;
    if (!this.flagSize) {
      c.width = c.offsetWidth;
      c.height = c.offsetHeight;
      this.flagSize = true;
    }
    var ctx = c.getContext('2d');
    if (ctx != null) {
      var elem;
      var id = '';
      var groups = '';
      if (group == 0) {
        groups = 'left_';
        id = 'left_' + index;
      } else if (group == 1) {
        groups = 'right_';
        id = 'right_' + index;
      }
      elem = document.getElementById(id);
      if (elem != null) {
        var bx = c.getBoundingClientRect();
        var bounds = elem.getBoundingClientRect();
        var x = bounds.left - bx.left + bounds.width / 2;
        var y = bounds.top - bx.top + bounds.height / 2;

        if (this.flagDrawing == 0) {
          this.idx = index;
          this.group = groups;
          this.flagDrawing = 1;
        } else {
          if (groups != this.group) {
            var link = new ConnectionLink();
            if (groups == 'right_') {
              link.setOrigin(this.idx);
              link.setDestiny(index);
            } else {
              link.setOrigin(index);
              link.setDestiny(this.idx);
            }
            this.sQuestionDetails.connections.push(link);
            var elem2 = document.getElementById(this.group + this.idx);
            if (elem2 != null) {
              var bx2 = c.getBoundingClientRect();
              var bounds2 = elem2.getBoundingClientRect();
              var x2 = bounds2.left - bx2.left + bounds2.width / 2;
              var y2 = bounds2.top - bx2.top + bounds2.height / 2;
              ctx.beginPath();
              ctx.moveTo(x2, y2);
              ctx.lineTo(x, y);
              ctx.lineWidth = 5;
              ctx.stroke();
              ctx.closePath();
              this.flagDrawing = 0;
            }
          } else {
            this.idx = index;
            this.group = groups;
          }
        }
      }
    }
  }

  redrawAllLines() {
    var c = document.getElementById('canvas') as HTMLCanvasElement;
    c.width = c.offsetWidth;
    c.height = c.offsetHeight;

    var ctx = c.getContext('2d');
    for (var item in this.sQuestionDetails.connections) {
      var item2 = parseInt(item);
      this.redrawLine(item2, '#000000');
    }
  }

  redrawLine(item: number, color: string) {
    var c = document.getElementById('canvas') as HTMLCanvasElement;
    var ctx = c.getContext('2d');
    var p = this.sQuestionDetails.connections[item];
    if (ctx != null) {
      var elem = document.getElementById('left_' + p.origin);
      if (elem != null) {
        var bx = c.getBoundingClientRect();
        var bounds = elem.getBoundingClientRect();
        var x = bounds.left - bx.left + bounds.width / 2;
        var y = bounds.top - bx.top + bounds.height / 2;
        var elem2 = document.getElementById('right_' + p.destiny);
        if (elem2 != null) {
          var bx2 = c.getBoundingClientRect();
          var bounds2 = elem2.getBoundingClientRect();
          var x2 = bounds2.left - bx2.left + bounds2.width / 2;
          var y2 = bounds2.top - bx2.top + bounds2.height / 2;
          ctx.beginPath();
          ctx.lineWidth = 5;
          ctx.moveTo(x2, y2);
          ctx.lineTo(x, y);
          ctx.strokeStyle = color;
          ctx.stroke();
          ctx.closePath();
        }
      }
    }
  }

  addItem(group: number) {
    if (group == 0) {
      this.sQuestionDetails.groupLeft.push(new ConnectionItem());
    } else if (group == 1) {
      this.sQuestionDetails.groupRight.push(new ConnectionItem());
    }
    setTimeout(() => {
      this.redrawAllLines();
    }, 1);
  }

  removeItem(group: number, index: number) {
    if (group == 0) {
      this.sQuestionDetails.groupLeft.splice(index, 1);
    } else if (group == 1) {
      this.sQuestionDetails.groupRight.splice(index, 1);
    }
    var res = [];
    for (var item in this.sQuestionDetails.connections) {
      var p = this.sQuestionDetails.connections[item];
      if (group == 0) {
        if (p.origin != index) {
          res.push(p);
          if (p.origin > index) {
            p.setOrigin(p.origin - 1);
          }
        }
      } else if (group == 1) {
        if (p.destiny != index) {
          res.push(p);
          if (p.destiny > index) {
            p.setDestiny(p.destiny - 1);
          }
        }
      }
    }
    this.sQuestionDetails.connections = res;
    setTimeout(() => {
      this.redrawAllLines();
    }, 1);
  }

  hover(event: MouseEvent) {
    var c = document.getElementById('canvas') as HTMLCanvasElement;
    var ctx = c.getContext('2d');
    var bx1 = c.getBoundingClientRect();
    var x1 = event.clientX - bx1.left;
    var y1 = event.clientY - bx1.top;
    this.hoveredLines = [];
    if (ctx != null) {
      for (var item in this.sQuestionDetails.connections) {
        var item2 = parseInt(item);
        var p = this.sQuestionDetails.connections[item2];
        var elem = document.getElementById('left_' + p.origin);
        if (elem != null) {
          var bx = c.getBoundingClientRect();
          var bounds = elem.getBoundingClientRect();
          var x = bounds.left - bx.left + bounds.width / 2;
          var y = bounds.top - bx.top + bounds.height / 2;
          var elem2 = document.getElementById('right_' + p.destiny);
          if (elem2 != null) {
            var bx2 = c.getBoundingClientRect();
            var bounds2 = elem2.getBoundingClientRect();
            var x2 = bounds2.left - bx2.left + bounds2.width / 2;
            var y2 = bounds2.top - bx2.top + bounds2.height / 2;
            ctx.beginPath();
            ctx.moveTo(x2, y2);
            ctx.lineTo(x, y);
            ctx.strokeStyle = 'None';
            if (ctx.isPointInStroke(x1, y1)) {
              this.hoveredLines.push(item2);
              this.redrawLine(item2, '#FF0000');
            } else {
              this.redrawLine(item2, '#000000');
            }
            ctx.closePath();
          }
        }
      }
    }
  }

  removeConnections(event: Event) {
    for (var i = this.hoveredLines.length - 1; i >= 0; i--) {
      this.sQuestionDetails.connections.splice(this.hoveredLines[i], 1);
    }
    setTimeout(() => {
      this.redrawAllLines();
    }, 1);
  }

  editItem(group: number, index: number) {
    this.editingGroup = group;
    this.editingIndex = index;
    if (group == 0) {
      this.editingContent = this.sQuestionDetails.groupLeft[index].content;
    } else if (group == 1) {
      this.editingContent = this.sQuestionDetails.groupRight[index].content;
    }
    this.editItemDialog = false;
    this.editItemDialog = true;
  }

  onClose() {
    this.editItemDialog = false;
  }

  onSave(content: string) {
    if (this.editingGroup == 0) {
      this.sQuestionDetails.groupLeft[this.editingIndex].content = content;
    } else if (this.editingGroup == 1) {
      this.sQuestionDetails.groupRight[this.editingIndex].content = content;
    }
    this.editItemDialog = false;
    setTimeout(() => {
      this.redrawAllLines();
    }, 1);
  }

  mounted() {
    var c = document.getElementById('canvas') as HTMLCanvasElement;
    c.width = c.width;
    c.height = c.height;
    setTimeout(() => {
      this.redrawAllLines();
    }, 300);
  }
}
</script>
