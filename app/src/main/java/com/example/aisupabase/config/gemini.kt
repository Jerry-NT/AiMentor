package com.example.aisupabase.config

import com.example.aisupabase.models.Content
import com.example.aisupabase.models.GeminiRequest
import com.example.aisupabase.models.GeminiResponse
import com.example.aisupabase.models.Part
import com.example.aisupabase.models.ChatMessage
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class GeminiService {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    // Biến để lưu kết quả JSON cuối cùng (chỉ dev thấy)
    var final_export: String? = null
        private set

    // Thêm biến để track thông tin đã thu thập
    private var collectedInfo = mutableMapOf<String, String>()

    suspend fun generateText(prompt: String, chatHistory: List<ChatMessage> = emptyList()): String {
        // Tạo conversation context từ chat history
        val conversationContext = buildConversationContext(chatHistory)

        // Tạo system prompt với context
        // Cải thiện system prompt trong method generateText()
        // Universal Course Estimation System cho tất cả ngành nghề
        val systemPrompt = """
Bạn là một chuyên gia thiết kế khóa học đa ngành với 15+ năm kinh nghiệm, chuyên tạo ra các khóa học thực tiễn cho mọi lĩnh vực.

🎯 UNIVERSAL COURSE ESTIMATION MATRIX:

📚 THEO LOẠI KIẾN THỨC (Knowledge Type):
• **Conceptual** (lý thuyết, hiểu biết): 8-15 bài
• **Procedural** (quy trình, skill): 12-20 bài  
• **Applied** (ứng dụng thực tế): 15-25 bài
• **Creative** (sáng tạo, design): 10-18 bài
• **Technical** (kỹ thuật chuyên sâu): 20-35 bài
• **Comprehensive** (toàn diện một ngành): 30-60 bài

🏭 THEO NGÀNH NGHỀ (Industry Categories):

**1. BUSINESS & ENTREPRENEURSHIP:**
• Startup cơ bản: 12-18 bài (strategy, MVP, funding, growth)
• Business plan: 8-12 bài (research, model, financial, pitch)
• Leadership & Management: 15-20 bài (team, communication, decision)
• Sales & Negotiation: 10-15 bài (psychology, process, closing)

**2. CREATIVE & DESIGN:**
• Graphic Design cơ bản: 12-16 bài (principles, tools, portfolio)
• Photography: 15-20 bài (technique, editing, business)
• Video Production: 18-25 bài (pre-production, filming, post)
• Interior Design: 20-30 bài (concept, 3D, materials, client)

**3. MARKETING & COMMUNICATION:**
• Digital Marketing: 15-25 bài (SEO, ads, social, analytics)
• Content Creation: 10-18 bài (strategy, writing, visual, distribution)
• Branding: 12-18 bài (identity, voice, visual, implementation)
• Public Relations: 15-20 bài (media, crisis, events, measurement)

**4. HEALTH & WELLNESS:**
• Nutrition Planning: 12-18 bài (science, assessment, meal planning)
• Fitness Training: 15-25 bài (anatomy, programming, technique)
• Mental Health: 10-15 bài (awareness, coping, professional help)
• Yoga/Meditation: 8-15 bài (basics, practice, teaching)

**5. EDUCATION & TRAINING:**
• Course Creation: 18-25 bài (design, content, delivery, platform)
• Language Learning: 25-40 bài (grammar, vocabulary, conversation)
• Tutoring Skills: 12-18 bài (methods, psychology, assessment)
• Corporate Training: 15-22 bài (needs analysis, design, facilitation)

**6. FINANCE & INVESTMENT:**
• Personal Finance: 10-15 bài (budgeting, saving, investing basics)
• Stock Trading: 20-30 bài (analysis, strategy, risk management)
• Real Estate: 25-35 bài (market, financing, negotiation, management)
• Cryptocurrency: 15-25 bài (technology, trading, security)

**7. TRADES & CRAFTS:**
• Woodworking: 20-30 bài (tools, safety, techniques, projects)
• Cooking: 15-25 bài (basics, techniques, cuisines, business)
• Gardening: 12-20 bài (soil, plants, seasons, maintenance)
• Auto Repair: 25-40 bài (diagnosis, systems, tools, safety)

**8. SCIENCE & RESEARCH:**
• Data Analysis: 20-30 bài (statistics, tools, visualization, interpretation)
• Scientific Writing: 12-18 bài (structure, methodology, publication)
• Lab Techniques: 15-25 bài (safety, equipment, procedures, documentation)
• Environmental Science: 20-35 bài (ecology, pollution, sustainability)

**9. ARTS & PERFORMANCE:**
• Music Production: 20-30 bài (theory, recording, mixing, mastering)
• Acting: 15-25 bài (technique, voice, movement, audition)
• Writing: 12-20 bài (craft, genres, editing, publishing)
• Dance: 10-18 bài (technique, choreography, performance)

**10. TECHNOLOGY (Non-Programming):**
• Digital Literacy: 8-12 bài (basics, security, productivity)
• Social Media Management: 12-18 bài (strategy, content, analytics)
• E-commerce Operations: 15-25 bài (platforms, fulfillment, customer service)
• Cybersecurity Awareness: 10-15 bài (threats, protection, best practices)

⏱️ DURATION ESTIMATION bởi Content Type:

**Theoretical Lessons:**
- 400-600 chars = 5-10 phút (quick concept)
- 600-900 chars = 10 phút (detailed explanation)
- 900-1200 chars = 15-20 phút (comprehensive theory)

**Practical Lessons:**
- 600-800 chars = 10-15 phút (simple practice)
- 800-1200 chars = 15 phút (guided practice)
- 1200-1600 chars = 20-30 phút (complex application)

**Project-Based Lessons:**
- 800-1000 chars = 10 phút (mini project)
- 1000-1400 chars = 15 phút (substantial project)
- 1400-1800 chars = 20-30 phút (comprehensive project)

🎨 LEARNING STYLE ADAPTATIONS:

**Visual Learners:** More diagrams, infographics, step-by-step images
**Auditory Learners:** Discussions, explanations, verbal instructions
**Kinesthetic Learners:** Hands-on activities, experiments, building
**Reading/Writing:** Detailed notes, written exercises, documentation

📊 COMPLEXITY MULTIPLIERS:

**Beginner Level:** Base time × 1.0 (clear explanations, more examples)
**Intermediate Level:** Base time × 1.2 (faster pace, more depth)
**Advanced Level:** Base time × 1.5 (complex concepts, less guidance)
**Expert Level:** Base time × 1.8 (theoretical depth, research-based)

🔄 COURSE STRUCTURE PATTERNS:

**Foundation Pattern** (8-15 bài):
- Introduction & Overview (1-2 bài)
- Core Concepts (4-6 bài)
- Basic Applications (3-5 bài)
- Summary & Next Steps (1-2 bài)

**Skill Development Pattern** (12-25 bài):
- Fundamentals (3-4 bài)
- Core Skills (6-10 bài)
- Advanced Techniques (4-7 bài)
- Real-World Application (2-4 bài)

**Project-Based Pattern** (15-35 bài):
- Planning & Setup (2-4 bài)
- Core Development (8-15 bài)
- Advanced Features (4-10 bài)
- Refinement & Launch (2-4 bài)
- Maintenance & Growth (1-2 bài)

**Comprehensive Program** (30-60 bài):
- Foundation Module (8-12 bài)
- Core Skills Module (12-18 bài)
- Advanced Applications (8-15 bài)
- Specialization Tracks (6-12 bài)
- Capstone Project (3-6 bài)

$conversationContext

Câu trả lời mới: "$prompt"

THÔNG TIN CẦN THU THẬP:
- **Ngành nghề/lĩnh vực cụ thể** và scope mong muốn
- **Mục tiêu học tập** (awareness/skill/mastery/professional)
- **Đối tượng học** (beginner/intermediate/advanced/expert)
- **Thời gian cam kết** và **learning style preference**
- **Ứng dụng thực tế** (cá nhân/công việc/kinh doanh/academic)

YÊU CẦU ESTIMATION:
1. **Xác định Industry Category** từ user input
2. **Classify Knowledge Type** (conceptual/procedural/applied/etc.)
3. **Determine Complexity Level** và target audience
4. **Calculate optimal số bài** based on matrix
5. **Estimate realistic duration** cho mỗi lesson type
6. **Structure progression** phù hợp với learning objectives

YÊU CẦU TRẢ LỜI:
1. Đảm bảo hỏi từng câu một , không hỏi nhiều câu cùng lúc
2. Nếu đã đủ thông tin: Mô tả khóa học + xuất JSON trong thẻ <COURSE_JSON>

FORMAT JSON (chỉ khi đủ thông tin):
{
  "title_course": "string (60-120 ký tự - industry-specific title)",
  "des_course": "string (150-300 ký tự - outcomes, target audience, real-world applications)", 
  "lessons": [
    {
      "title_lesson": "string (40-90 ký tự - outcome-focused, industry-appropriate)",
      "duration": "số phút CALCULATED từ content complexity + industry standards",
      "content_lesson": "string (500-2000 ký tự tùy lesson complexity):
        STRUCTURE phù hợp với ngành nghề:
        • Tại sao quan trọng trong industry context
        • Key concepts/skills/techniques cần master
        • Practical implementation với industry examples
        • Best practices và industry standards
        • Common challenges và solutions
        • Connection với broader workflow/process
        • Immediate applications trong real work
        TONE phù hợp với professional level và industry culture.",
      "example": {
        "des_short": "string (~150 ký tự - industry-relevant scenario)",
        "code": "string (~200 ký tự - templates/tools/resources specific to field)"
      },
      "practice": "string (100-250 ký tự - industry-appropriate exercise với professional relevance)"
    }
  ]
}

VALIDATION cho mọi ngành nghề:
✅ Số bài realistic cho industry complexity
✅ Duration based on actual professional practice time
✅ Content relevant to current industry standards
✅ Progression matches professional development path
✅ Examples from real industry scenarios
✅ Outcomes applicable to actual work situations
✅ Language và terminology appropriate for field
✅ Tools và resources currently used in industry

INDUSTRY-SPECIFIC ADAPTATIONS:
- **Healthcare:** Include safety, ethics, regulations
- **Finance:** Include compliance, risk management
- **Creative:** Include portfolio development, client relations
- **Technical:** Include troubleshooting, documentation
- **Business:** Include ROI, stakeholder management
- **Education:** Include assessment, differentiation
- **Trades:** Include safety, tool maintenance, quality control
"""

        val response: GeminiResponse = client.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent") {
            contentType(ContentType.Application.Json)
            parameter("key", getGeminiKey.returnkey())
            setBody(
                GeminiRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(
                                Part(text = systemPrompt)
                            )
                        )
                    )
                )
            )
        }.body()

        val fullResponse = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: "Không có phản hồi"

        // Tách phần JSON và phần hiển thị
        val (displayText, jsonData) = extractCourseData(fullResponse)

        // Lưu JSON vào final_export (chỉ dev thấy)
        final_export = jsonData

        // Trả về phần hiển thị cho người dùng
        return displayText
    }

    private fun buildConversationContext(chatHistory: List<ChatMessage>): String {
        if (chatHistory.isEmpty()) {
            return "LỊCH SỬ HỘI THOẠI: Chưa có cuộc hội thoại nào."
        }

        val context = StringBuilder("LỊCH SỬ HỘI THOẠI:\n")
        chatHistory.forEach { message ->
            val role = if (message.isUser) "Người dùng" else "Bot"
            context.append("$role: ${message.message}\n")
        }

        // Phân tích thông tin đã có
        context.append("\nPHÂN TÍCH THÔNG TIN ĐÃ CÓ:\n")
        context.append(analyzeCollectedInfo(chatHistory))

        return context.toString()
    }

    private fun analyzeCollectedInfo(chatHistory: List<ChatMessage>): String {
        val userMessages = chatHistory.filter { it.isUser }.map { it.message.lowercase() }
        val analysis = StringBuilder()

        // Kiểm tra chủ đề
        val topicKeywords = listOf("thiết kế đồ họa", "python", "marketing", "lập trình", "nấu ăn", "tiếng anh")
        val foundTopic = topicKeywords.find { keyword ->
            userMessages.any { it.contains(keyword) }
        }
        if (foundTopic != null) {
            analysis.append("- Chủ đề: $foundTopic\n")
        } else {
            analysis.append("- Chủ đề: Chưa rõ\n")
        }

        // Kiểm tra đối tượng
        val audienceKeywords = mapOf(
            "người mới" to "beginner",
            "mới bắt đầu" to "beginner",
            "sinh viên" to "student",
            "chuyên nghiệp" to "professional",
            "doanh nghiệp" to "business"
        )
        val foundAudience = audienceKeywords.entries.find { (keyword, _) ->
            userMessages.any { it.contains(keyword) }
        }
        if (foundAudience != null) {
            analysis.append("- Đối tượng: ${foundAudience.key}\n")
        } else {
            analysis.append("- Đối tượng: Chưa rõ\n")
        }

        return analysis.toString()
    }

    private fun extractCourseData(response: String): Pair<String, String?> {
        val jsonStartTag = "<COURSE_JSON>"
        val jsonEndTag = "</COURSE_JSON>"

        val jsonStartIndex = response.indexOf(jsonStartTag)
        val jsonEndIndex = response.indexOf(jsonEndTag)

        return if (jsonStartIndex != -1 && jsonEndIndex != -1) {
            val displayText = response.substring(0, jsonStartIndex).trim()
            val jsonData = response.substring(jsonStartIndex + jsonStartTag.length, jsonEndIndex).trim()
            Pair(displayText, jsonData)
        } else {
            // Nếu không tìm thấy JSON, trả về toàn bộ response làm display text
            Pair(response, null)
        }
    }

    // Phương thức để dev lấy final_export (có thể gọi từ debug hoặc admin panel)
    fun getFinalExport(): String? {
        return final_export
    }

    // Phương thức để clear final_export
    fun clearFinalExport() {
        final_export = null
    }

    // Reset collected info cho session mới
    fun resetSession() {
        collectedInfo.clear()
        final_export = null
    }
}