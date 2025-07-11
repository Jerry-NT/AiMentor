package com.example.aisupabase.config

import Question
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
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import android.util.Base64
import android.util.Log
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonArray
import java.io.File

@Serializable
data class QuestionsResponse(
    val questions: List<Question>
)


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

    // Biến để lưu URL ảnh course
    private var courseImageUrl: String? = null


    suspend fun generateText(prompt: String, chatHistory: List<ChatMessage> = emptyList()): String {
        // Tạo conversation context từ chat history
        val conversationContext = buildConversationContext(chatHistory)
        val collectedInfoAnalysis = analyzeCollectedInfo(chatHistory)

        val systemPrompt = """
        🚀 **BẠN LÀ CHUYÊN GIA TẠO KHÓA HỌC IT THÔNG MINH**
        
        ═══════════════════════════════════════════════════════════════════════════════════════
        
        ## 🎯 **NHIỆM VỤ CHÍNH**
        Thu thập thông tin chi tiết và tạo khóa học IT chất lượng cao, thực tế và có tính ứng dụng
        
        ## 📊 **PHÂN TÍCH TÌNH HUỐNG HIỆN TẠI**
        $collectedInfoAnalysis
        
        ## 🔍 **THÔNG TIN CẦN THU THẬP (Theo độ ưu tiên)**
        
        ### **Tier 1 - Thông tin cốt lõi (BẮT BUỘC)**
        1. **Chủ đề chính**: Lĩnh vực IT cụ thể (Web Dev, Mobile, AI/ML, DevOps, Data Science, Cybersecurity)
        2. **Mục tiêu học tập**: 
           - 🎯 Awareness (Hiểu biết cơ bản)
           - 🛠️ Skill-building (Xây dựng kỹ năng)
           - 🏆 Mastery (Thành thạo)
           - 💼 Career-ready (Sẵn sàng nghề nghiệp)
        3. **Trình độ hiện tại**: 
           - 🌱 Absolute Beginner (Chưa biết gì)
           - 📚 Some Knowledge (Có kiến thức cơ bản)
           - 🔧 Intermediate (Trung bình)
           - ⚡ Advanced (Nâng cao)
        
        ### **Tier 2 - Thông tin tùy chỉnh (QUAN TRỌNG)**
        4. **Thời gian cam kết**: 
           - ⚡ Crash Course (1-2 tuần)
           - 📖 Standard (3-4 tuần)
           - 🎓 Comprehensive (2-3 tháng)
           - 🏆 Mastery (6+ tháng)
        5. **Phong cách học**: 
           - 📖 Theory-focused (Lý thuyết)
           - 🛠️ Hands-on (Thực hành)
           - 📊 Project-based (Dự án)
           - 🎮 Interactive (Tương tác)
        
        ### **Tier 3 - Thông tin nâng cao (TỐI ƯU)**
        6. **Ứng dụng thực tế**: 
           - 🏠 Personal Projects
           - 💼 Current Job
           - 🚀 Career Switch
           - 📈 Business/Startup
        7. **Công cụ/Framework ưu tiên**: Yêu cầu specific tools
        8. **Ngân sách thời gian**: Số giờ/tuần có thể học
        
        ## 🧠 **CHIẾN LƯỢC HỎI THÔNG TIN THÔNG MINH**
        
        ### **Giai đoạn 1: Khám phá (Discovery)**
        - Sử dụng câu hỏi mở: "Bạn muốn làm gì với kiến thức này?"
        - Tìm hiểu motivation: "Điều gì khiến bạn quan tâm đến [chủ đề]?"
        - Đánh giá background: "Bạn đã từng tiếp xúc với [related topic] chưa?"
        
        ### **Giai đoạn 2: Làm rõ (Clarification)**
        - Đi sâu vào details: "Khi nói về [topic], bạn có muốn tập trung vào [specific aspect]?"
        - Scenarios: "Ví dụ, bạn có muốn học để [use case A] hay [use case B]?"
        - Timeline: "Bạn muốn đạt được mục tiêu này trong khoảng thời gian nào?"
        
        ### **Giai đoạn 3: Xác nhận (Confirmation)**
        - Tóm tắt: "Vậy tôi hiểu bạn muốn [summary]..."
        - Double-check: "Có đúng là bạn ưu tiên [priority] hơn [alternative]?"
        - Final check: "Còn gì khác tôi cần biết không?"
        
        ## 📚 **CHUẨN TẠO KHÓA HỌC CHẤT LƯỢNG CAO**
        
        ### **1. Cấu trúc khóa học (Course Structure)**
        - **Tiêu đề**: [10-248 chars] - Hấp dẫn, SEO-friendly, nói rõ value proposition
        - **Mô tả**: [10-498 chars] - Giải thích rõ outcomes, benefits, target audience
        - **Số bài học**: 4-10 bài (tùy theo complexity và timeline)
        - **Tổng thời gian**: Realistic estimation dựa trên content depth
        
        ### **2. Thiết kế bài học (Lesson Design)**
        - **Tiêu đề bài**: [15-248 chars] - Action-oriented, clear learning outcome
        - **Thời lượng**: Calculated scientifically (xem công thức bên dưới)
        - **Cấu trúc**: Theory → Example → Practice → Assessment
        - **Thumbnail**: Professional images từ Pexels
        
        ### **3. Nội dung chi tiết (Content Details)**
        - **Sections**: 2-5 phần/bài học
        - **Content Description**: [250-1500 chars] - Comprehensive, actionable
        - **Code Examples**: Real-world, working code với comments
        - **Visuals**: Relevant images supporting learning
        - **Practical Tips**: Industry best practices
        
        ### **4. Hệ thống đánh giá (Assessment System)**
        - **3 câu hỏi essay/bài**: Varied difficulty levels
        - **Scenario-based**: Real-world problem solving
        - **Progressive**: Building on previous knowledge
        - **Actionable**: Encouraging practical application
        
        ## 🧮 **CÔNG THỨC TÍNH THỜI LƯỢNG KHOA HỌC**
        
        ### **Tốc độ đọc chuẩn**:
        - Text tiếng Việt: 220 từ/phút
        - Code reading: 120 từ/phút
        - Technical documentation: 180 từ/phút
        
        ### **Thời gian bổ sung**:
        - Xem hình/diagram: 0.8 phút/hình
        - Suy nghĩ câu hỏi: 2.5 phút/câu
        - Pause/reflection: 5% tổng thời gian
        - Code practice: 1.5x reading time
        
        ### **Công thức tính toán**:
        ```
        duration = (total_text_words / 220) + 
                   (total_code_words / 120) + 
                   (image_count * 0.8) + 
                   (question_count * 2.5) + 
                   (pause_reflection_time)
        ```
        
        ### **Làm tròn**: Lên 5 phút gần nhất (min: 25 phút, max: 120 phút)
        
        
        ## 📋 **FORMAT JSON RESPONSE BẮT BUỘC**
        
        **KHI ĐÃ ĐỦ THÔNG TIN, PHẢI TRẢ VỀ JSON THEO FORMAT SAU:**
        
        ```json
        {
          "course_title": "Tên khóa học (10-248 ký tự)",
          "course_description": "Mô tả khóa học (10-498 ký tự)",
          "lessons": [
            {
              "lesson_title": "Tiêu đề bài học (15-248 ký tự)",
              "duration": duration, // Tính toán theo công thức trên
              "content_lesson": [
                {
                  "content_title": "Tiêu đề phần nội dung",
                  "content_description": "Nội dung chi tiết (250-1500 ký tự)",
                  "example": {
                    "example_description": "Mô tả ví dụ ngắn gọn (~150 ký tự)",
                    "code_example": "console.log('Hello World'); // Mã code minh họa (~150 ký tự) - Bỏ trường này nếu không phải lập trình"
                  }
                }
              ],
              "practice_questions": [
                {
                  "question": "Câu hỏi thực hành số 1 (15-248 ký tự)",
                  "type": "essay"
                },
                {
                  "question": "Câu hỏi thực hành số 2 (15-248 ký tự)",
                  "type": "essay"
                },
                {
                  "question": "Câu hỏi thực hành số 3 (15-248 ký tự)",
                  "type": "essay"
                }
              ]
            }
          ]
        }
        ```
        
        ## 🔧 **IMPLEMENTATION SPECIFICS**
        
        ### **JSON Structure Requirements**:
        - Validate all required fields
        - Ensure data types match schema
        - Include calculation notes
        - Error handling for edge cases
        
        ### **Quality Assurance**:
        - Word count accuracy
        - Duration calculation verification
        - Content relevance check
        - Technical accuracy review
        
        ## 🎯 **QUY TẮC TRẢ LỜI**
        
        ### **Khi chưa đủ thông tin**:
        1. **HỎI MỘT CÂU DUY NHẤT** - Không overwhelm user
        2. **Contextual questions** - Dựa vào thông tin đã có
        3. **Provide options** - Đưa ra 2-3 lựa chọn cụ thể
        4. **Explain why** - Giải thích tại sao cần thông tin này
        5. **KHÔNG TRẢ VỀ JSON** - Chỉ hỏi thêm thông tin
        
        ### **Khi đã đủ thông tin**:
        1. **Tóm tắt ngắn gọn** - Recap thông tin đã thu thập (1-2 câu)
        2. **TRẢ VỀ JSON NGAY** - Theo đúng format đã chỉ định
        3. **JSON phải hoàn chỉnh** - Không được thiếu field nào
        4. **Tính toán chính xác** - Duration phải đúng công thức
        
        ### **QUAN TRỌNG - Response Format**:
        - **Nếu chưa đủ thông tin**: Chỉ trả lời text bình thường
        - **Nếu đã đủ thông tin**: Trả về JSON hoàn chỉnh theo format đã chỉ định
        - **Xuất JSON** - Trong thẻ `<COURSE_JSON>...</COURSE_JSON>`
        
        ### **Tone và Style**:
        - 🎯 Professional nhưng friendly
        - 💡 Proactive suggestions
        - 🔍 Detail-oriented
        - 🚀 Encouraging và motivational
        
        ## 📖 **SAMPLE QUESTIONS BY CONTEXT**
        
        ### **For Beginners**:
        - "Bạn có muốn tập trung vào việc hiểu concepts trước, hay bạn thích học qua thực hành luôn?"
        - "Bạn có thời gian khoảng bao nhiêu giờ mỗi tuần để học?"
        
        ### **For Intermediate**:
        - "Bạn muốn nâng cao kỹ năng hiện tại hay mở rộng sang lĩnh vực mới?"
        - "Có framework hoặc tool nào bạn đặc biệt quan tâm không?"
        
        ### **For Advanced**:
        - "Bạn muốn deep-dive vào architecture hay focus vào practical implementation?"
        - "Mục tiêu cuối cùng là gì - teaching others, leading team, hay personal mastery?"
        
        ═══════════════════════════════════════════════════════════════════════════════════════
        
        $conversationContext
        
        **Tin nhắn mới từ user**: $prompt
        
        **Hãy phản hồi một cách thông minh. Nếu chưa đủ thông tin thì hỏi thêm. Nếu đã đủ thông tin thì trả về JSON theo format đã chỉ định.**
        
    """.trimIndent()

        val response: GeminiResponse =
            client.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent") {
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

        // Kiểm tra xem response có phải là JSON không
        val jsonData = extractJsonFromResponse(fullResponse)

        if (jsonData != null) {
            // Lưu JSON để dev có thể truy cập
            final_export = jsonData

            // Tạo ảnh course thumbnail
            val courseTitle = kotlinx.serialization.json.Json.parseToJsonElement(jsonData)
                .jsonObject["course_title"]?.jsonPrimitive?.content ?: "IT Course"

            // Tạo ảnh bằng Imagen API
            generateCourseImage(courseTitle)

            // Trả về message thông báo hoàn thành cho user (KHÔNG trả về JSON)
            return """
                🎉 **Khóa học đã được tạo thành công!**
                
                📚 **Tên khóa học**: $courseTitle
                
                ✅ **Hoàn thành**: 
                - Cấu trúc khóa học chi tiết
                - Nội dung bài học đã được thiết kế
                - Câu hỏi thực hành đã chuẩn bị
                - Ảnh thumbnail đã tạo xong
                
                🚀 **Khóa học của bạn đã sẵn sàng để sử dụng!**
                
                Bạn có muốn điều chỉnh gì thêm không?
            """.trimIndent()
        } else {
            // Trả về text response bình thường (khi chưa đủ thông tin)
            return fullResponse
        }
    }

    suspend fun generateCourseImage(courseTitle: String) {
        try {
            // Alternative: Create JSON string directly
            val requestBodyJson = """
       {
    "contents": [{
      "parts": [
        {"text": "Hello, can you create a  ${courseTitle}?"}
      ]
    }],
    "generationConfig":{"responseModalities":["TEXT","IMAGE"]}
  }
        """.trimIndent()

            // Call the correct Gemini endpoint
            val response: String =
                client.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-preview-image-generation:generateContent") {
                    contentType(ContentType.Application.Json)
                    parameter("key", getGeminiKey.returnkey())
                    setBody(requestBodyJson)
                }.bodyAsText()

            Log.d("GeminiServiceIMG", "Image generation response: $response")

            // Parse the response to extract base64 image data
            val json = kotlinx.serialization.json.Json.parseToJsonElement(response).jsonObject

            // Navigate through the response structure to find the image data
            val candidates = json["candidates"]?.jsonArray
            if (candidates != null && candidates.isNotEmpty()) {
                val firstCandidate = candidates[0].jsonObject
                val content = firstCandidate["content"]?.jsonObject
                val parts = content?.get("parts")?.jsonArray

                var base64Image: String? = null

                // Look for image data in the parts
                parts?.forEach { part ->
                    val partObj = part.jsonObject
                    val inlineData = partObj["inlineData"]?.jsonObject
                    if (inlineData != null) {
                        base64Image = inlineData["data"]?.jsonPrimitive?.content
                    }
                }

                // If base64 image data is found
                if (base64Image != null) {
                    val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
                    val tempFile = File.createTempFile("course_thumbnail", ".png")
                    tempFile.writeBytes(imageBytes)
                    courseImageUrl =
                        com.example.aisupabase.cloudinary.CloudinaryService.uploadImage(tempFile)
                    tempFile.delete()

                    Log.d("GeminiService", "Course image generated successfully: $courseImageUrl")
                } else {
                    Log.e("GeminiService", "No image data found in response")
                    courseImageUrl = null
                }
            } else {
                Log.e("GeminiService", "No candidates found in response")
                courseImageUrl = null
            }

        } catch (e: Exception) {
            Log.e("GeminiService", "Error generating course image: ${e.message}")
            e.printStackTrace()
            courseImageUrl = null
        }
    }

    private fun buildConversationContext(chatHistory: List<ChatMessage>): String {
        if (chatHistory.isEmpty()) {
            return "**LỊCH SỬ HỘI THOẠI**: Cuộc trò chuyện mới bắt đầu."
        }

        val context = StringBuilder("**LỊCH SỬ HỘI THOẠI**:\n")
        chatHistory.takeLast(10).forEach { message -> // Chỉ lấy 10 tin nhắn gần nhất
            val role = if (message.isUser) "👤 User" else "🤖 Assistant"
            context.append("$role: ${message.message}\n")
        }

        return context.toString()
    }

    private fun analyzeCollectedInfo(chatHistory: List<ChatMessage>): String {
        val userMessages = chatHistory.filter { it.isUser }.map { it.message.lowercase() }
        val analysis = StringBuilder("**PHÂN TÍCH THÔNG TIN ĐÃ THU THẬP**:\n\n")

        // Phân tích chủ đề IT
        val itTopics = mapOf(
            "web development" to listOf(
                "web",
                "html",
                "css",
                "javascript",
                "react",
                "vue",
                "angular",
                "frontend",
                "backend"
            ),
            "mobile development" to listOf(
                "mobile",
                "android",
                "ios",
                "flutter",
                "react native",
                "kotlin",
                "swift"
            ),
            "data science" to listOf(
                "data",
                "analytics",
                "python",
                "machine learning",
                "ai",
                "statistics",
                "pandas"
            ),
            "devops" to listOf(
                "devops",
                "docker",
                "kubernetes",
                "aws",
                "cloud",
                "ci/cd",
                "jenkins"
            ),
            "cybersecurity" to listOf(
                "security",
                "ethical hacking",
                "penetration",
                "network security",
                "firewall"
            ),
            "game development" to listOf(
                "game",
                "unity",
                "unreal",
                "c#",
                "gamedev",
                "3d",
                "animation"
            )
        )

        var foundTopic = "❓ Chưa xác định"
        for ((topic, keywords) in itTopics) {
            if (keywords.any { keyword -> userMessages.any { it.contains(keyword) } }) {
                foundTopic = "✅ $topic"
                break
            }
        }
        analysis.append("📚 **Chủ đề**: $foundTopic\n")

        // Phân tích trình độ
        val skillLevels = mapOf(
            "Beginner" to listOf("mới bắt đầu", "chưa biết", "cơ bản", "học từ đầu", "beginner"),
            "Intermediate" to listOf(
                "trung bình",
                "có kinh nghiệm",
                "intermediate",
                "đã biết một chút"
            ),
            "Advanced" to listOf("nâng cao", "advanced", "expert", "chuyên nghiệp", "thành thạo")
        )

        var foundLevel = "❓ Chưa xác định"
        for ((level, keywords) in skillLevels) {
            if (keywords.any { keyword -> userMessages.any { it.contains(keyword) } }) {
                foundLevel = "✅ $level"
                break
            }
        }
        analysis.append("🎯 **Trình độ**: $foundLevel\n")

        // Phân tích mục tiêu
        val goals = mapOf(
            "Career Switch" to listOf("chuyển nghề", "tìm việc", "career", "job", "công việc mới"),
            "Skill Enhancement" to listOf("nâng cao", "improve", "better", "skill", "kỹ năng"),
            "Personal Interest" to listOf("sở thích", "personal", "hobby", "tò mò", "quan tâm"),
            "Business Need" to listOf("doanh nghiệp", "business", "startup", "project", "dự án")
        )

        var foundGoal = "❓ Chưa xác định"
        for ((goal, keywords) in goals) {
            if (keywords.any { keyword -> userMessages.any { it.contains(keyword) } }) {
                foundGoal = "✅ $goal"
                break
            }
        }
        analysis.append("🎯 **Mục tiêu**: $foundGoal\n")

        // Phân tích timeline
        val timelines = mapOf(
            "Rush (1-2 tuần)" to listOf("gấp", "nhanh", "rush", "1 tuần", "2 tuần"),
            "Standard (3-4 tuần)" to listOf("bình thường", "3 tuần", "4 tuần", "1 tháng"),
            "Comprehensive (2-3 tháng)" to listOf(
                "kỹ lưỡng",
                "2 tháng",
                "3 tháng",
                "comprehensive"
            ),
            "Long-term (6+ tháng)" to listOf("dài hạn", "6 tháng", "1 năm", "long term")
        )

        var foundTimeline = "❓ Chưa xác định"
        for ((timeline, keywords) in timelines) {
            if (keywords.any { keyword -> userMessages.any { it.contains(keyword) } }) {
                foundTimeline = "✅ $timeline"
                break
            }
        }
        analysis.append("⏰ **Timeline**: $foundTimeline\n")

        // Đánh giá độ hoàn thiện thông tin
        val completionScore =
            listOf(foundTopic, foundLevel, foundGoal, foundTimeline).count { it.startsWith("✅") }
        val completionPercentage = (completionScore * 25)
        analysis.append("\n📊 **Độ hoàn thiện thông tin**: $completionPercentage% ($completionScore/4 tiêu chí)\n")

        when {
            completionPercentage >= 75 -> analysis.append("🟢 **Trạng thái**: Đủ thông tin để tạo khóa học!\n")
            completionPercentage >= 50 -> analysis.append("🟡 **Trạng thái**: Cần thêm 1-2 thông tin nữa\n")
            else -> analysis.append("🔴 **Trạng thái**: Cần thu thập thêm thông tin cơ bản\n")
        }

        return analysis.toString()
    }

    private fun extractJsonFromResponse(response: String): String? {
        val trimmedResponse = response.trim()

        // Kiểm tra xem response có chứa JSON trong <COURSE_JSON> tags
        val jsonStartTag = "<COURSE_JSON>"
        val jsonEndTag = "</COURSE_JSON>"

        if (trimmedResponse.contains(jsonStartTag) && trimmedResponse.contains(jsonEndTag)) {
            val startIndex = trimmedResponse.indexOf(jsonStartTag) + jsonStartTag.length
            val endIndex = trimmedResponse.indexOf(jsonEndTag, startIndex)
            if (endIndex > startIndex) {
                return trimmedResponse.substring(startIndex, endIndex).trim()
            }
        }

        // Kiểm tra xem response có bắt đầu bằng { và kết thúc bằng } không
        if (trimmedResponse.startsWith("{") && trimmedResponse.endsWith("}")) {
            return trimmedResponse
        }

        // Tìm kiếm JSON object trong response
        val jsonStartIndex = trimmedResponse.indexOf("{")
        val jsonEndIndex = trimmedResponse.lastIndexOf("}")

        if (jsonStartIndex != -1 && jsonEndIndex != -1 && jsonStartIndex < jsonEndIndex) {
            val potentialJson = trimmedResponse.substring(jsonStartIndex, jsonEndIndex + 1)

            // Kiểm tra xem có phải JSON hợp lệ không
            return try {
                kotlinx.serialization.json.Json.parseToJsonElement(potentialJson)
                potentialJson
            } catch (e: Exception) {
                null
            }
        }

        return null
    }

    // Phương thức để dev lấy final_export (có thể gọi từ debug hoặc admin panel)
    fun getFinalExport(): String? {
        return final_export
    }

    // Phương thức để lấy URL ảnh course
    fun getCourseImageUrl(): String? {
        return courseImageUrl
    }

    suspend fun generateQuestion(prompt: String): Result<List<Question>> {
        return try {
            val systemPrompt = """
        Bạn là một chuyên gia tạo câu hỏi trắc nghiệm cho các khóa học IT. 
        Hãy tạo ra 5 câu hỏi trắc nghiệm chất lượng cao dựa trên nội dung: "$prompt"
        
        ## 📋 YÊU CẦU CHẤT LƯỢNG:
        1. **Độ khó tăng dần**: Câu 1-2 (cơ bản), Câu 3-4 (trung bình), Câu 5 (nâng cao)
        2. **Tính thực tế**: Câu hỏi phải áp dụng được trong công việc thực tế
        3. **Đáp án hợp lý**: Các đáp án sai phải hợp lý, không quá dễ loại bỏ
        4. **Kiến thức toàn diện**: Bao phủ các khía cạnh khác nhau của chủ đề
        5. **Tránh lỗi**: Kiểm tra kỹ tính chính xác của đáp án đúng
        
        ## 🎯 ĐỊNH DẠNG RESPONSE (CHỈ TRẢ VỀ JSON):
        ```json
        {
          "questions": [
            {
              "question": "Câu hỏi cụ thể và rõ ràng (20-200 ký tự)",
              "A": "Đáp án A - chi tiết và chính xác",
              "B": "Đáp án B - chi tiết và chính xác", 
              "C": "Đáp án C - chi tiết và chính xác",
              "D": "Đáp án D - chi tiết và chính xác",
              "choice": "A" // Chữ cái đáp án đúng (A/B/C/D)
            }
          ]
        }
        ```
        
        ## ⚠️ LƯU Ý QUAN TRỌNG:
        - Chỉ trả về JSON thuần túy, không thêm markdown hoặc text giải thích
        - Trường "choice" phải chứa chữ cái (A/B/C/D), không phải nội dung đáp án
        - Đảm bảo tất cả câu hỏi đều liên quan chặt chẽ đến chủ đề đã cho
        - Kiểm tra kỹ tính chính xác của đáp án trước khi trả về
        
        Hãy tạo 5 câu hỏi chất lượng cao ngay bây giờ.
        """.trimIndent()

            val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

            val response: GeminiResponse = client.post(GEMINI_BASE_URL) {
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
                ?: throw Exception("Không có phản hồi từ Gemini API")

            Log.d("GeminiService", "Raw response: $fullResponse")

            // Làm sạch response để chỉ lấy JSON
            val cleanedResponse = cleanJsonResponse(fullResponse)
            Log.d("GeminiService", "Cleaned response: $cleanedResponse")

            // Parse JSON response với error handling tốt hơn
            val questionsResponse = try {
                Json.decodeFromString<QuestionsResponse>(cleanedResponse)
            } catch (e: Exception) {
                Log.e("GeminiService", "JSON parsing error: ${e.message}")
                throw Exception("Lỗi phân tích dữ liệu từ AI. Vui lòng thử lại.")
            }

            // Validate câu hỏi
            val validatedQuestions = validateQuestions(questionsResponse.questions)

            if (validatedQuestions.isEmpty()) {
                throw Exception("Không thể tạo câu hỏi hợp lệ. Vui lòng thử lại.")
            }

            Log.d("GeminiService", "Generated ${validatedQuestions.size} valid questions")
            Result.success(validatedQuestions)

        } catch (e: Exception) {
            Log.e("GeminiService", "Error generating questions: ${e.message}", e)
            Result.failure(Exception("Lỗi tạo câu hỏi: ${e.message}"))
        }
    }

    // Hàm làm sạch JSON response
    private fun cleanJsonResponse(response: String): String {
        var cleaned = response.trim()

        // Loại bỏ markdown code blocks
        cleaned = cleaned.replace("```json", "").replace("```", "")

        // Loại bỏ text phía trước JSON
        val jsonStart = cleaned.indexOf("{")
        val jsonEnd = cleaned.lastIndexOf("}")

        if (jsonStart != -1 && jsonEnd != -1 && jsonStart <= jsonEnd) {
            cleaned = cleaned.substring(jsonStart, jsonEnd + 1)
        }

        return cleaned.trim()
    }

    // Hàm validate câu hỏi
    private fun validateQuestions(questions: List<Question>): List<Question> {
        return questions.filter { question ->
            // Kiểm tra các điều kiện cơ bản
            question.question.isNotBlank() &&
                    question.A.isNotBlank() &&
                    question.B.isNotBlank() &&
                    question.C.isNotBlank() &&
                    question.D.isNotBlank() &&
                    question.choice.matches(Regex("[ABCD]")) &&
                    question.question.length in 20..200
        }.take(5) // Chỉ lấy tối đa 5 câu
    }

}