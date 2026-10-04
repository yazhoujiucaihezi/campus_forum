<script setup>
import {Quill, QuillEditor} from "@vueup/vue-quill";
import '@vueup/vue-quill/dist/vue-quill.snow.css';
import ImageResize from "quill-image-resize-vue";
import {ImageExtend, QuillWatch} from "quill-image-super-solution-module";
import {Check, Close} from "@element-plus/icons-vue";
import {ref} from "vue";
import axios from "axios";
import {accessHeader} from "@/net";
import {apiForumCommentSubmit} from "@/net/api/forum";
import {ElMessage} from "element-plus";

const props = defineProps({
  show: Boolean,
  tid: Number,
  quote: Object
})

const emit = defineEmits(['close', 'comment'])

const editor = ref('')
const refEditor = ref()

Quill.register('modules/imageResize', ImageResize)
Quill.register('modules/ImageExtend', ImageExtend)

const editorOption = {
  modules: {
    toolbar: {
      container: [
        "bold", "italic", "underline", "strike",
        {color: []}, {'background': []},
        {list: "ordered"}, {list: "bullet"},
        "link", "image"
      ],
      handlers: {
        'image': function () {
          QuillWatch.emit(this.quill.id)
        }
      }
    },
    imageResize: {
      modules: ['Resize', 'DisplaySize']
    },
    ImageExtend: {
      action: axios.defaults.baseURL + '/api/image/cache',
      name: 'file',
      size: 6,
      loading: true,
      accept: 'image/png, image/jpeg',
      response: (resp) => {
        if (resp.data) {
          return axios.defaults.baseURL + '/images' + resp.data
        } else {
          return null
        }
      },
      methods: 'POST',
      headers: xhr => {
        xhr.setRequestHeader('Authorization', accessHeader().Authorization);
      },
      start: () => editor.loading = true,
      success: () => {
        ElMessage.success('图片上传成功!')
      },
      error: () => {
        ElMessage.warning('图片上传失败，请联系管理员!')
      }
    }
  }
}

function submitComment() {
  if (!editor.value || !editor.value.ops) {
    ElMessage.warning('请填写评论内容！')
    return
  }
  apiForumCommentSubmit({
    tid: props.tid,
    content: JSON.stringify(editor.value),
    quote: props.quote ? props.quote.id : -1
  }, () => {
    ElMessage.success('评论发表成功！')
    editor.value = ''
    emit('comment')
    emit('close')
  })
}
</script>

<template>
  <el-dialog :model-value="show" @close="emit('close')" title="发表评论" width="600px">
    <div style="height: 200px">
      <quill-editor v-model:content="editor" content-type="delta" ref="refEditor"
                    :options="editorOption" placeholder="请输入评论内容..."/>
    </div>
    <template #footer>
      <div style="display: flex; justify-content: space-between; align-items: center">
        <div style="font-size: 12px; color: grey" v-if="quote">
          回复：{{ quote.user?.username }}
        </div>
        <div>
          <el-button @click="emit('close')" :icon="Close" plain>取消</el-button>
          <el-button @click="submitComment" type="success" :icon="Check" plain>发表评论</el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
:deep(.ql-editor) {
  min-height: 150px;
}
:deep(.ql-editor img) {
  max-width: 100% !important;
  height: auto !important;
}
</style>