package extract.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EngineKtTest {
    @Test
    fun assignLabelWithPatterns() {
        val label = assignLabel(
                testCommit(title = "Open created actual class in editor #KT-20135 Fixed"),
                Extract("YouTrack", "^.*(KT-\\d+).*$", null, arrayOf(),
                        "path", "\${1}", "\${0}", "https://youtrack.jetbrains.com/issue/\${1}")
        )

        assertEquals(
                ExtractLabel(
                        "YouTrack", "KT-20135",
                        hint = "Open created actual class in editor #KT-20135 Fixed",
                        icon = "path",
                        url = "https://youtrack.jetbrains.com/issue/KT-20135",
                        style = null,
                        badges = arrayOf(),
                        labelName = null),
                label
        )
    }

    @Test
    fun assignLabelWithMessagePattern() {
        val title = "Open created actual class in editor"
        val label = assignLabel(
                testCommit(hash = "123", title = title, message = "$title\n\n #KT-20135 Fixed"),
                Extract("YouTrack", null, "^.*(KT-\\d+).*$", arrayOf(),
                        "path", "\${1}", "\${1}", "https://youtrack.jetbrains.com/issue/\${1}")
        )

        assertEquals(
                ExtractLabel(
                        "YouTrack", "KT-20135",
                        hint = "KT-20135",
                        icon = "path",
                        url = "https://youtrack.jetbrains.com/issue/KT-20135",
                        style = null,
                        badges = arrayOf(),
                        labelName = null),
                label
        )
    }

    @Test
    fun assignLabelByBothPatterns() {
        val extract =
                Extract("Some",
                        titlePattern = "^.*(first).*$",
                        messagePattern = "^.*(second).*$",
                        text = "\${1}")

        assertEquals(
                ExtractLabel(
                        name = "Some", text = "first", icon = null, hint = null, url = null,
                        style = null, badges = arrayOf(), labelName = null),
                assignLabel(
                        testCommit(hash = "123", title = "bla bla bla first", message = "bla bla bla first\n\nfoo foo foo first second"),
                        extract)
        )

        assertEquals(
                ExtractLabel(
                        name = "Some", text = "second", icon = null, hint = null, url = null,
                        style = null, badges = arrayOf(), labelName = null),
                assignLabel(
                        testCommit(hash = "345", title = "bla bla bla", message = "bla bla bla\n\nfoo foo foo first second"),
                        extract)
        )
    }

    @Test
    fun fileMatchTest() {
        assertTrue(pathMatch(".idea/some", arrayOf(".idea/**")))
        assertTrue(pathMatch("Changes.md", arrayOf("Changes.md")))
        assertTrue(pathMatch("one/two/ui/three", arrayOf("**/ui/**")))
    }

    @Test
    fun matchesVariable() {
        val label = assignLabel(
                testCommit(
                        title = "Some",
                        fileActions = arrayOf(
                                FileAction(Action.ADD, "first.test"),
                                FileAction(Action.MODIFY, "second.test"),
                                FileAction(Action.RENAME, "second.other")
                        )),
                Extract("YouTrack",
                        titlePattern = null, files = arrayOf("**.test"),
                        icon = "path",
                        text = "\${matches}\\\${count}",
                        hint = "\${matches}\\\${count}",
                        url = "\${matches}\\\${count}",
                        badge = "\${matches}\\\${count}")
        )

        assertEquals(
                ExtractLabel(
                        "YouTrack",
                        text = "2\\3",
                        hint = "2\\3",
                        icon = "path",
                        url = "2\\3",
                        style = null,
                        badges = arrayOf("2\\3"),
                        labelName = null),
                label
        )
    }

    @Test
    fun assignNamedLabel() {
        val label = assignLabel(
                testCommit(
                        title = "Some",
                        fileActions = arrayOf(FileAction(Action.ADD, "first.test"))),

                Extract("WithLabel",
                        files = arrayOf("**.test"),
                        labelName = "With Label")
        )

        assertEquals(
                ExtractLabel(
                        "WithLabel",
                        text = null,
                        hint = null,
                        icon = null,
                        url = null,
                        style = null,
                        badges = arrayOf(),
                        labelName = "With Label"),
                label
        )
    }
}

private val dummyUser = User("dummyUserName", "dummyUserEmail")

fun testCommit(
        hash: String = "dummyHash",
        parentHashes: Array<String> = arrayOf(),
        author: User = dummyUser,
        committer: User = dummyUser,
        time: Int = 0,
        title: String = "dummyTitle",
        message: String = "dummyMessage",
        fileActions: Array<FileAction> = arrayOf()
        ) = CommitInfo(hash, parentHashes, author, committer, time, title, message, fileActions)