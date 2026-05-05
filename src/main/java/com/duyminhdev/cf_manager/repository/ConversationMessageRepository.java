package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.ConversationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {

    /**
     * Lấy tối đa 10 tin nhắn gần nhất của một phiên chat theo thứ tự thời gian tăng dần.
     */
    List<ConversationMessage> findTop10ByUserIdAndSessionIdOrderByCreatedAtAsc(
            String userId, String sessionId);
}
