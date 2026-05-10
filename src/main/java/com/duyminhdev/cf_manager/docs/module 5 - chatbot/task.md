# AI Chatbot Module — Task Tracker

## Phase 1: Config & Setup
- [x] Task tracker
- [ ] `application.properties` — thêm AI config + cafe.info
- [ ] `AiConfig.java` — ChatClient bean
- [ ] `CafeInfoProperties.java` — @ConfigurationProperties

## Phase 2: Entity & DB
- [ ] `ConversationMessage.java`
- [ ] `UserPreference.java`
- [ ] `V_chatbot_tables.sql`

## Phase 3: Repository & DTO
- [ ] `ConversationMessageRepository.java`
- [ ] `UserPreferenceRepository.java`
- [ ] `ChatRequest.java`
- [ ] `ChatResponse.java`
- [ ] `TableAvailabilityDTO.java`
- [ ] `SalesSummaryDTO.java`
- [ ] `IngredientStockDTO.java`
- [ ] `NativeSqlChatRepository.java` (interface)
- [ ] `NativeSqlChatRepositoryImpl.java`

## Phase 4: Helpers
- [ ] `IntentType.java` (enum)
- [ ] `ChatTimeUtils.java`
- [ ] `TableQueryParser.java`
- [ ] `BookingParser.java`

## Phase 5: Service
- [ ] `AiChatService.java` (interface)
- [ ] `AiChatServiceImpl.java`

## Phase 6: Controller & Security
- [ ] `AiChatController.java`
- [ ] `SecurityConfig.java` (thêm whitelist /api/v1/chat/public)
