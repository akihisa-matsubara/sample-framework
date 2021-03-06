package dev.sample.framework.core.interceptor;

import dev.sample.framework.core.message.CoreMessageId;
import dev.sample.framework.core.util.CdiUtils;
import dev.sample.framework.core.util.MessageUtils;
import java.io.Serializable;
import javax.annotation.Priority;
import javax.enterprise.context.Conversation;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Conversation実行インターセプター.
 */
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
@Interceptor
@InvokeConversation
@Slf4j
public class InvokeConversationInterceptor implements Serializable {

  /** serialVersionUID. */
  private static final long serialVersionUID = -5859736536223450434L;

  /** Conversation タイムアウト(30m). */
  private static final long CONVERSATION_TIMEOUT_MILLISEC = 30 * 60 * 1000L;

  /**
   * Conversationの開始／終了を実行します.
   *
   * @param context InvocationContext
   * @return Object
   * @throws Exception 例外
   */
  @AroundInvoke
  public Object invoke(InvocationContext context) throws Exception {
    InvokeConversation annotation = context.getMethod().getAnnotation(InvokeConversation.class);

    if (InvokeConversation.Type.START == annotation.type()) {
      startConversation();
    }

    try {
      return context.proceed();

    } finally {
      if (InvokeConversation.Type.END == annotation.type()) {
        endConversation();
      }
    }
  }

  /**
   * Conversationを開始します.
   */
  private void startConversation() {
    Conversation conv = CdiUtils.getBean(Conversation.class);
    if (conv.isTransient()) {
      conv.begin();
      // timeout default 10m -> 30m (> session timeout)
      conv.setTimeout(CONVERSATION_TIMEOUT_MILLISEC);
      log.info(MessageUtils.getMessage(CoreMessageId.F0010I), conv.getId());
    }
  }

  /**
   * Conversationを終了します.
   */
  private void endConversation() {
    Conversation conv = CdiUtils.getBean(Conversation.class);
    if (!conv.isTransient()) {
      conv.end();
      log.info(MessageUtils.getMessage(CoreMessageId.F0011I), conv.getId());
    }
  }

}
