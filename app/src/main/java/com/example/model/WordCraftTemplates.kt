package com.example.model

import java.util.UUID

object WordCraftTemplates {

    fun createSampleDocument(): WordCraftDoc {
        val blocks = listOf(
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.TITLE,
                text = "The Open Studio Handbook",
                alignment = TextAlignment.LEFT
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.SUBTITLE,
                text = "How a small community of makers shares space, tools and time",
                alignment = TextAlignment.LEFT
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.PARAGRAPH,
                text = "Every Thursday evening the old print shop on Alder Street fills with people who make things. Painters set up by the north windows, letterpress regulars claim the back room, and somebody always brings fresh sourdough bread. This handbook collects what we have learned about running a shared studio: the routines that keep it pleasant, the tools we pool, and the small agreements that let thirty strangers become a crew."
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.HEADING_1,
                text = "Why share a studio?"
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.PARAGRAPH,
                text = "Rent is the obvious reason, but it is not the best one. A shared room puts your craft next to work you would never have encountered otherwise. A ceramicist explains glaze chemistry to a comic artist; a photographer borrows a weaver's loom for a still life. The cross-talk is the real point of WordCraft."
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.QUOTE,
                text = "A studio is a place where unfinished things are warmly welcomed."
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.HEADING_2,
                text = "What we agree on"
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.BULLET_LIST,
                text = "Clean as you go. Leave a workbench the way you would like to find it."
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.BULLET_LIST,
                text = "Label what is yours. Unlabelled supplies on the commons shelf are considered community stock."
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.BULLET_LIST,
                text = "Ask before you borrow. Even from the shared shelf, a quick note helps everyone keep track."
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.BULLET_LIST,
                text = "Respect the quiet room. Conversations, music, and sanding happen in the front hall."
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.CALLOUT,
                text = "Important Safety Reminder: All heavy power machinery must be completely powered down and locked at the master switch by 10:00 PM nightly.",
                calloutType = CalloutType.WARNING
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.HEADING_2,
                text = "Studio Workstations & Schedule"
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.TABLE,
                text = "",
                tableData = TableData(
                    headers = listOf("Station", "Equipment", "Host", "Available Hours"),
                    rows = listOf(
                        listOf("Print Shop", "Vandercook No. 4, Proof press", "Clara M.", "Tue & Thu 6-10 PM"),
                        listOf("Darkroom", "Omega D5 Enlarger, Chemistry", "Tomas K.", "Mon & Wed 5-9 PM"),
                        listOf("Ceramics", "Shimpo Wheels, Skutt Kiln", "Aisha R.", "Daily 9 AM - 8 PM"),
                        listOf("Woodworking", "Cabinet Saw, Festool Router", "David L.", "Fri & Sat 10 AM - 6 PM")
                    ),
                    style = TableStyle.BANDED_BLUE
                )
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.HEADING_2,
                text = "Daily Closing Checklist"
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.CHECKLIST,
                text = "Dust and wipe down all shared work surfaces",
                isChecked = true
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.CHECKLIST,
                text = "Ensure ventilation blowers and solvent bins are securely closed",
                isChecked = true
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.CHECKLIST,
                text = "Lock front entrance and deposit keys in safety lockbox",
                isChecked = false
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.PARAGRAPH,
                text = "For emergency maintenance or lockouts, contact the Alder Street studio cooperative coordinator via the bulletin board hotline."
            )
        )

        return WordCraftDoc(
            id = "sample_handbook",
            title = "The Open Studio Handbook",
            author = "WordCraft Studio",
            blocks = blocks,
            watermark = null,
            headerText = "The Open Studio Handbook • Alder Street Co-op",
            footerText = "WordCraft Open Document Format",
            showPageNumbers = true
        )
    }

    fun createResumeTemplate(): WordCraftDoc {
        val blocks = listOf(
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.TITLE,
                text = "Alex Morgan",
                alignment = TextAlignment.CENTER
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.SUBTITLE,
                text = "Principal Software Architect • alex.morgan@example.com • (555) 019-2834 • Seattle, WA",
                alignment = TextAlignment.CENTER
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.HORIZONTAL_RULE,
                text = ""
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.HEADING_1,
                text = "Executive Summary"
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.PARAGRAPH,
                text = "Seasoned engineering leader with 10+ years architecting scalable cloud platforms, native mobile applications, and high-performance desktop systems. Proven track record growing distributed engineering teams."
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.HEADING_1,
                text = "Professional Experience"
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.HEADING_2,
                text = "Lead Systems Architect — CloudScale Inc. (2021 – Present)"
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.BULLET_LIST,
                text = "Spearheaded migration of legacy core services to low-latency microservices, reducing p99 latency by 42%."
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.BULLET_LIST,
                text = "Directed a cross-functional team of 18 engineers delivering global document synchronization engines."
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.HEADING_2,
                text = "Senior Software Engineer — Apex Systems (2017 – 2021)"
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.BULLET_LIST,
                text = "Designed and deployed native mobile client frameworks supporting offline caching and real-time editing."
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.HEADING_1,
                text = "Education & Certifications"
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.PARAGRAPH,
                text = "B.S. in Computer Science — University of Washington, Magna Cum Laude"
            )
        )

        return WordCraftDoc(
            id = "template_resume",
            title = "Executive Resume",
            author = "Alex Morgan",
            blocks = blocks,
            margins = PageMargins.MODERATE,
            headerText = "Alex Morgan — Curriculum Vitae",
            footerText = "Confidential",
            showPageNumbers = true
        )
    }

    fun createReportTemplate(): WordCraftDoc {
        val blocks = listOf(
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.TITLE,
                text = "Quarterly Research & Analysis Report",
                alignment = TextAlignment.LEFT
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.SUBTITLE,
                text = "An in-depth review of market trends, productivity software, and open architectures",
                alignment = TextAlignment.LEFT
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.HEADING_1,
                text = "1. Executive Briefing"
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.PARAGRAPH,
                text = "Modern information workers spend an average of 4.2 hours daily composing, reviewing, and formatting documents. The resurgence of clean-room, native desktop and mobile word processors demonstrates user appetite for speed and local data sovereignty."
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.CALLOUT,
                text = "Key Finding: Native compiled document tools exhibit 5x faster cold-start times and 80% lower memory usage compared to heavy web wrappers.",
                calloutType = CalloutType.TIP
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.HEADING_1,
                text = "2. Comparative Performance Matrix"
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.TABLE,
                text = "",
                tableData = TableData(
                    headers = listOf("Metric", "Legacy Suite", "WordCraft Native", "Variance"),
                    rows = listOf(
                        listOf("Cold Start (ms)", "1,850 ms", "120 ms", "-93.5%"),
                        listOf("Memory Usage", "450 MB", "48 MB", "-89.3%"),
                        listOf("DOCX Parse Time", "320 ms", "28 ms", "-91.2%"),
                        listOf("Battery Efficiency", "Moderate", "Optimal", "+4.2x")
                    ),
                    style = TableStyle.BANDED_BLUE
                )
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.HEADING_1,
                text = "3. Recommendations & Next Steps"
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.NUMBERED_LIST,
                text = "Standardize document interchange on open .docx and markdown formats."
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.NUMBERED_LIST,
                text = "Equip field staff with lightweight Android tablet and phone word processing tooling."
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.NUMBERED_LIST,
                text = "Adopt local-first persistence models to safeguard intellectual property."
            )
        )

        return WordCraftDoc(
            id = "template_report",
            title = "Research Report",
            author = "Research Division",
            blocks = blocks,
            headerText = "Quarterly Research & Analysis Report",
            footerText = "WordCraft Intelligence",
            showPageNumbers = true
        )
    }

    fun createLetterTemplate(): WordCraftDoc {
        val blocks = listOf(
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.PARAGRAPH,
                text = "WordCraft Studio\n1042 Alder Street, Suite 400\nPortland, OR 97205",
                alignment = TextAlignment.RIGHT
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.PARAGRAPH,
                text = "October 16, 2026\n\nRecipient Name\nCreative Arts Guild\n500 Broadway Avenue\nSeattle, WA 98102"
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.PARAGRAPH,
                text = "Dear Guild Members,"
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.PARAGRAPH,
                text = "We are pleased to introduce the mobile Android edition of WordCraft. Built on the principles of speed, beautiful typography, and full compatibility with standard DOCX documents, WordCraft brings the beloved desktop word processing ribbon workflow to your fingertips."
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.PARAGRAPH,
                text = "We look forward to collaborating on upcoming publications, design workshops, and community exhibitions."
            ),
            DocBlock(
                id = UUID.randomUUID().toString(),
                type = BlockType.PARAGRAPH,
                text = "Sincerely,\n\nThe WordCraft Core Team"
            )
        )

        return WordCraftDoc(
            id = "template_letter",
            title = "Formal Letter",
            author = "WordCraft Team",
            blocks = blocks,
            headerText = "WordCraft Correspondence",
            footerText = "",
            showPageNumbers = false
        )
    }

    fun createBlankDocument(): WordCraftDoc {
        return WordCraftDoc(
            id = UUID.randomUUID().toString(),
            title = "Untitled Document",
            author = "WordCraft User",
            blocks = listOf(
                DocBlock(
                    id = UUID.randomUUID().toString(),
                    type = BlockType.TITLE,
                    text = "Untitled Document"
                ),
                DocBlock(
                    id = UUID.randomUUID().toString(),
                    type = BlockType.PARAGRAPH,
                    text = "Start typing your document here..."
                )
            )
        )
    }
}
