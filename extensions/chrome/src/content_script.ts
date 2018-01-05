/// <reference> another-rest-client.d.ts
import * as RestClient from 'another-rest-client';

window.onload = function () {
    console.log("Extracts onload");
    ExtractsContentScript.modifyLog();
    ExtractsContentScript.fetchCommitData("JetBrains", "kotlin", "2cdc246a274ff3fff92a7c880a905945ed6c0833");
};

namespace ExtractsContentScript {
    const COMMIT_CLASS_NAME = "commit";
    const LINKS_CELL_CLASS_NAME = "commit-links-cell";
    const COMMIT_DATA_ATTRIBUTE = "data-channel";

    // language=RegExp
    const COMMIT_DATA_PATTERN = new RegExp("^repo:(\\w+):commit:(\\w+)$"); // repo:{number}:commit:{hash}

    export function modifyLog() {
        let commitsElements = document.getElementsByClassName(COMMIT_CLASS_NAME);
        let maxIndex = Math.min(commitsElements.length, 10);

        for (let i = 0; i < maxIndex; i++) {
            let commitElement = commitsElements.item(i);

            let linksCellElements = commitElement.getElementsByClassName(LINKS_CELL_CLASS_NAME);
            if (linksCellElements.length != 1) continue;

            let dataAttribute = commitElement.getAttribute(COMMIT_DATA_ATTRIBUTE);
            if (!dataAttribute) continue;

            let [repoId, hash] = parseCommitData(dataAttribute);
            console.log(`RepoId: ${repoId} Commit hash: ${hash}`);

            let linksCellElement = linksCellElements.item(0);
            linksCellElement.appendChild(createExtractTag("e1", "Dummy Title", "dummy"));
        }
    }

    function parseCommitData(commitData: string): [string, string] | null {
        let matched = COMMIT_DATA_PATTERN.exec(commitData);
        if (matched == null) return null;
        return [matched[1], matched[2]];
    }

    function createExtractTag(styleClass: string, title: string, extractText: string) {
        let tagSpan = document.createElement("span");
        tagSpan.className = "extract-tag " + styleClass;
        tagSpan.title = title;
        tagSpan.innerText = extractText;

        return tagSpan;
    }

    export function fetchCommitData(owner: string, repo: string, commitHash: string) {
        let api = new RestClient('https://api.github.com');
        api.res(
            {repos: ['releases', 'commits']}
        );

        api.repos(`${owner}/${repo}`).commits(commitHash).get().then(function (commitData) {
            console.log(commitData.commit.message);
        });
    }
}