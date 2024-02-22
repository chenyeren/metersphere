// eslint-disable-next-line no-shadow

import Item from './comment-item.vue';
import CommentInput from './input.vue';

import {useI18n} from '@/hooks/useI18n';

import {CommentEvent, CommentItem, CommentParams, CommentType} from './types';
import message from '@arco-design/web-vue/es/message';

export default defineComponent({
  name: 'MsComment',
  props: {
    commentList: {
      type: Array as PropType<CommentItem[]>,
      default: () => [],
    },
    disabled: {
      type: Boolean as PropType<boolean>,
      default: false,
    },
  },
  emits: {
    /* eslint-disable @typescript-eslint/no-unused-vars */
    updateOrAdd: (value: CommentParams, cb: (result: boolean) => void) => true, // 更新或者新增评论
    delete: (value: string) => true, // 删除评论
  },
  setup(props, { emit }) {
    const { commentList, disabled } = toRefs(props);
    const currentItem = reactive<{ id: string; commentType: CommentType; commentStatus: string }>({
      id: '',
      commentType: 'ADD',
      // 控制回复编辑删除按钮的状态
      commentStatus: 'normal',
    });
    // 被@的用户id
    const noticeUserIds = ref<string[]>([]);
    const { t } = useI18n();

    const resetCurrentItem = () => {
      currentItem.id = '';
      currentItem.commentType = 'ADD';
      currentItem.commentStatus = 'normal';
      noticeUserIds.value = [];
    };

    const handlePublish = (content: string, item: CommentItem) => {
      // 这个组件里的都是回复和编辑不涉及新增，所以是 COMMENT 或 REPLY
      let parentId = '';
      let event: CommentEvent = 'COMMENT';
      if (currentItem.commentType === 'REPLY') {
        parentId = item.id;
        event = 'REPLY';
      } else if (currentItem.commentType === 'EDIT') {
        parentId = item.parentId || '';
        if (noticeUserIds.value.length > 0) {
          event = 'AT';
        }
      }
      const params: CommentParams = {
        bugId: item.bugId,
        content,
        event,
        commentType: currentItem.commentType,
        fetchType: currentItem.commentType === 'EDIT' ? 'UPDATE' : 'ADD',
        notifier: noticeUserIds.value.join(';'),
        replyUser: item.createUser,
        parentId,
      };
      if (currentItem.commentType === 'EDIT') {
        params.id = item.id;
      }
      emit('updateOrAdd', params, (result: boolean) => {
        if (result) {
          message.success(t('common.publishSuccessfully'));
          resetCurrentItem();
        } else {
          message.error(t('common.publishFailed'));
        }
      });
    };

    const handleDelete = (item: CommentItem) => {
      emit('delete', item.id);
    };

    const handleReply = (item: CommentItem) => {
      if (item.childComments && Array.isArray(item.childComments)) {
        // 点击的是父级评论的回复
        currentItem.id = item.id;
      } else {
        // 子级评论
        currentItem.id = item.parentId || '';
      }
      currentItem.commentType = 'REPLY';
    };

    const handelEdit = (item: CommentItem) => {
      currentItem.id = item.id;
      currentItem.commentType = 'EDIT';
    };

    const renderInput = (item: CommentItem) => {
      return (
        <CommentInput
          isShowAvatar={false}
          isUseBottom={false}
          onPublish={(content: string) => handlePublish(content, item)}
          defaultValue={currentItem.commentType === 'EDIT' ? item.content : ''}
          noticeUserIds={noticeUserIds.value}
          onUpdate:noticeUserIds={(ids: string[]) => {
            noticeUserIds.value = ids;
          }}
          onCancel={() => resetCurrentItem()}
          {...item}
        />
      );
    };

    const renderChildrenList = (list?: CommentItem[]) => {
      if (!list || list.length === 0) {
        return null;
      }
      return list.map((item) => {
        return (
          <div class="flex flex-col">
            <Item
              mode={'child'}
              onReply={() => handleReply(item)}
              onEdit={() => handelEdit(item)}
              onDelete={() => handleDelete(item)}
              status={item.id === currentItem.id ? currentItem.commentStatus : 'normal'}
              onUpdate:status={(v: string) => {
                currentItem.commentStatus = v;
              }}
              element={item}
            />
            {item.id === currentItem.id && renderInput(item)}
          </div>
        );
      });
    };

    const renderParentList = (list: CommentItem[]) => {
      return list.map((item) => {
        return (
          <>
            <Item
              mode={'parent'}
              onReply={() => handleReply(item)}
              onEdit={() => handelEdit(item)}
              onDelete={() => handleDelete(item)}
              status={item.id === currentItem.id ? currentItem.commentStatus : 'normal'}
              onUpdate:status={(v: string) => {
                currentItem.commentStatus = v;
              }}
              element={item}
            >
              <div class="rounded border border-[var(--color-text-7)] p-[16px]">
                {renderChildrenList(item.childComments)}
              </div>
            </Item>
            {item.id === currentItem.id && renderInput(item)}
          </>
        );
      });
    };

    return () => <div class="ms-comment gap[24px] flex flex-col">{renderParentList(commentList.value)}</div>;
  },
});
