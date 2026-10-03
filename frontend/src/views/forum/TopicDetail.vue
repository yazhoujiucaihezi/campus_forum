<script setup>
    import {useRoute} from "vue-router";
    import {reactive, ref} from "vue";
    import {
    ArrowLeft,
    ChatSquare,
    CircleCheck,
    Delete,
    EditPen,
    Female,
    Lock,
    Male,
    Plus,
    Star
    } from "@element-plus/icons-vue";
    import { QuillDeltaToHtmlConverter } from 'quill-delta-to-html';
    import Card from "@/components/Card.vue";
    import router from "@/router";
    import TopicTag from "@/components/TopicTag.vue";
    import InteractButton from "@/components/InteractButton.vue";
    import {ElMessage} from "element-plus";
    import {useStore} from "@/store";
    import TopicEditor from "@/components/TopicEditor.vue";
    import TopicCommentEditor from "@/components/TopicCommentEditor.vue";
    import {
    apiForumCommentDelete,
    apiForumComments,
    apiForumInteract,
    apiForumTopic,
    apiForumUpdateTopic
    } from "@/net/api/forum";
    import { apiForumUserTopicDelete } from "@/net/api/forum"; 

    const route = useRoute()
    const store = useStore()
    const tid = route.params.tid

    const topic = reactive({
    data: null,
    like: false,
    collect: false,
    likeCount: 0,
    collectCount: 0,
    components: null,
    page: 1
    })
    const edit=ref(false)
    const comment = reactive({
    show: false,
    text: '',
    quote: null
    })

    const init =()=> apiForumTopic(tid,data=>{
    topic.data=data
    topic.like=data.interact.like
    topic.collect=data.interact.collect
    topic.likeCount=data.interact.likeCount
    topic.collectCount=data.interact.collectCount
    loadComments(1)
    })
    init()

    function convertToHtml(content){
    const ops = JSON.parse(content).ops
    const converter = new QuillDeltaToHtmlConverter(ops, { inlineStyles: true });
    return converter.convert();
    }

    function interact(type, message) {
  if (type === 'like') {
    topic.likeCount += topic.like ? -1 : 1
  } else if (type === 'collect') {
    topic.collectCount += topic.collect ? -1 : 1
  }
  apiForumInteract(tid, type, topic, message)
}

    function updateTopic(editor) {
    apiForumUpdateTopic({
    id: tid,
    type: editor.type.id,
    title: editor.title,
    content: JSON.stringify(editor.text)
    }, () => {
    ElMessage.success('帖子内容更新成功！')
    edit.value = false
    init()
    })
    }

    function loadComments(page) {
    topic.comments = null
    topic.page = page
    apiForumComments(tid, page - 1, data => topic.comments = data)
    }

    function onCommentAdd() {
    comment.show = false
    loadComments(Math.floor(++topic.data.comments / 10) + 1)
    }

    function deleteComment(id) {
    apiForumCommentDelete(id, () => {
    ElMessage.success('删除评论成功！')
    loadComments(topic.page)
    })
    }

    function deleteTopic() {
       if (!confirm('确定删除这个帖子吗？')) return
       apiForumUserTopicDelete(tid, () => {
        ElMessage.success('删除成功')
        router.push('/index')
       })
}
</script>

<template>
    <div class="topic-page" v-if="topic.data">
        <div class="topic-main" style="position: sticky;top: 0;z-index: 10">
            <card style="display: flex;width: 100%">
                <el-button :icon="ArrowLeft" type="info" size="small"
                           plain round @click="router.push('/index')">返回列表</el-button>
                <div style="text-align: center;flex: 1">
                    <el-tag size="small" effect="dark" type="warning" disable-transitions
                            style="margin-right: 10px;" v-if="topic.data.locked">
                        <el-icon>
                            <Lock />
                        </el-icon>
                        已锁定
                    </el-tag>
                    <topic-tag :type="topic.data.type" />
                    <span style="font-weight: bold;margin-left: 5px">{{topic.data.title}}</span>
                </div>
            </card>
        </div>
        <div class="topic-main">
            <div class="topic-main-left">
                <el-avatar :src="store.avatarUserUrl(topic.data.user.avatar)"
                           :size="60" />
                <div>
                    <div style="font-size: 18px;font-weight: bold">
                        {{topic.data.user.username}}
                        <span style="color: hotpink" v-if="topic.data.user.gender === 1">
                            <el-icon><Female /></el-icon>
                        </span>
                        <span style="color: dodgerblue" v-if="topic.data.user.gender === 0">
                            <el-icon><Male /></el-icon>
                        </span>
                    </div>
                    <div class="desc">{{topic.data.user.email}}</div>
                </div>
                <el-divider style="margin: 10px 0" />
                <div style="text-align: left;margin: 0 5px">
                    <div class="desc">微信号: {{topic.data.user.wx || '已隐藏或未填写'}}</div>
                    <div class="desc">QQ号: {{topic.data.user.qq || '已隐藏或未填写'}}</div>
                    <div class="desc">手机号: {{topic.data.user.phone || '已隐藏或未填写'}}</div>
                </div>
                <el-divider style="margin: 10px 0" />
                <div class="desc" style="margin: 0 5px">{{topic.data.user.desc}}</div>
            </div>
            <div class="topic-main-right">
                <div class="topic-content" v-html="convertToHtml(topic.data.content)"></div>
                <el-divider />
                <div style="font-size: 13px;color: grey;text-align: center">
                    <div style="text-align: right">发帖时间: {{new Date(topic.data.time).toLocaleString()}}</div>
                </div>
                <div style="text-align: right;margin-top: 30px">
                    <interact-button name="编辑帖子" color="dodgerblue" :check="false"
                                     @check="edit=true" style="margin-right: 20px"
                                     :disabled="topic.data.locked"
                                     v-if="store.user.id===topic.data.user.id">
                        <el-icon><EditPen /></el-icon>
                    </interact-button>

                    <interact-button name="删除帖子" color="red" :check="false"
                                     @check="deleteTopic" style="margin-right: 20px"
                                     v-if="store.user.id===topic.data.user.id || store.user.role==='admin'">
                        <el-icon><Delete /></el-icon>
                    </interact-button>

                    <interact-button :name="`点个赞吧 (${topic.likeCount})`" :check-name="`已点赞 (${topic.likeCount})`" color="pink" :check="topic.like"
                                     @check="interact('like','点赞')">
                        {{ topic.like ? '❤️' : '🤍' }}
                    </interact-button>
                    <interact-button :name="`收藏本帖 (${topic.collectCount})`" :check-name="`已收藏 (${topic.collectCount})`" color="orange" :check="topic.collect"
                                     @check="interact('collect','收藏')"
                                     style="margin-left: 20px">
                        {{ topic.collect ? '⭐' : '☆' }}
                    </interact-button>
                </div>
            </div>
        </div>
        <transition name="el-fade-in-linear" mode="out-in">
            <div v-if="topic.comments">
                <div class="topic-main" style="margin-top: 10px" v-for="item in topic.comments">
                    <div class="topic-main-left">
                        <el-avatar :src="store.avatarUserUrl(item.user.avatar)"
                                   :size="60" />
                        <div>
                            <div style="font-size: 18px;font-weight: bold">
                                {{item.user.username}}
                                <span style="color: hotpink" v-if="item.user.gender === 1">
                                    <el-icon><Female /></el-icon>
                                </span>
                                <span style="color: dodgerblue" v-if="item.user.gender === 0">
                                    <el-icon><Male /></el-icon>
                                </span>
                            </div>
                            <div class="desc">{{item.user.email}}</div>
                        </div>
                        <el-divider style="margin: 10px 0" />
                        <div style="text-align: left;margin: 0 5px">
                            <div class="desc">微信号: {{item.user.wx || '已隐藏或未填写'}}</div>
                            <div class="desc">QQ号: {{item.user.qq || '已隐藏或未填写'}}</div>
                            <div class="desc">手机号: {{item.user.phone || '已隐藏或未填写'}}</div>
                        </div>
                    </div>
                    <div class="topic-main-right">
                        <div>

                        </div>
                        <div style="font-size: 13px;color: grey">
                            <div>评论时间: {{new Date(item.time).toLocaleString()}}</div>
                        </div>
                        <div v-if="item.quote" class="comment-quote">
                            回复: {{item.quote}}
                        </div>
                        <div class="topic-content" v-html="convertToHtml(item.content)"></div>
                        <div style="text-align: right">
                            <el-link :icon="ChatSquare" @click="comment.show = true;comment.quote = item"
                                     type="info">&nbsp;回复评论</el-link>
                            <el-link :icon="Delete" type="danger"
                                     v-if="item.user.id === store.user.id || topic.data.user.id === store.user.id || store.user.role === 'admin'"
                                     style="margin-left: 20px" @click="deleteComment(item.id)">&nbsp;删除评论</el-link>
                        </div>
                    </div>
                </div>
                <div style="width: fit-content;margin: 20px auto">
                    <el-pagination background layout="prev, pager, next"
                                   v-model:current-page="topic.page" @current-change="loadComments"
                                   :total="topic.data.comments" :page-size="10"
                                   hide-on-single-page />
                </div>
            </div>
        </transition>
        <topic-editor :show="edit" @close="edit =false" v-if="topic.data&&store.forum.types"
                      :default-type="topic.data.type" :default-text="topic.data.content"
                      :default-title="topic.data.title" submit-button="更新帖子内容" :submit="updateTopic" />
        <topic-comment-editor :show="comment.show" @close="comment.show=false" :tid="tid"
                              :quote="comment.quote" @comment="onCommentAdd" />
        <div class="add-comment" v-if="!comment.show && !edit"
             @click="comment.show=true;comment.quote = null">
            <el-icon><Plus /></el-icon>
        </div>
    </div>
</template>

<style scoped>
.comment-quote {
  font-size: 13px;
  color: grey;
  background-color: rgba(94, 94, 94, 0.1);
  padding: 10px;
  margin-top: 10px;
  border-radius: 5px;
}

.add-comment {
  position: fixed;
  bottom: 40px;
  right: 40px;
  width: 80px;
  height: 40px;
  border-radius: 20px;
  font-size: 14px;
  color: #fff;
  text-align: center;
  line-height: 40px;
  background: #409eff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
  cursor: pointer;
  z-index: 9999;
}

.add-comment:hover {
  background: #66b1ff;
}

.topic-page {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 10px 0;
}

.topic-main {
  display: flex;
  border-radius: 7px;
  margin: 0 auto;
  background-color: var(--el-bg-color);
  width: 800px;
}

.topic-main-left {
  width: 200px;
  padding: 10px;
  text-align: center;
  border-right: solid 1px var(--el-border-color);
}

.topic-main-left .desc {
  font-size: 12px;
  color: grey;
}

.topic-main-right {
  width: 600px;
  padding: 10px 20px;
  display: flex;
  flex-direction: column;
}

.topic-content {
  font-size: 14px;
  line-height: 22px;
  opacity: 0.8;
  flex: 1;
  word-break: break-word;
}

.topic-content :deep(img) {
  max-width: 100% !important;
  width: auto !important;
  height: auto !important;
  display: block;
}
</style>
<style>
.topic-content img {
  max-width: 100% !important;
  max-height: 500px !important;
  width: auto !important;
  height: auto !important;
  display: block;
  object-fit: contain;
}
</style>