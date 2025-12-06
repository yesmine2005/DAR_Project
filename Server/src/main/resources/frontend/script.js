class VulnerabilityAnalyzer {
    constructor() {
        this.analyzeBtn = document.getElementById('analyzeBtn');
        this.codeInput = document.getElementById('code');
        this.languageSelect = document.getElementById('language');
        this.resultDiv = document.getElementById('result');
        this.loadingDiv = document.getElementById('loading');

        this.initializeEvents();
        this.createMatrixBackground();
    }

    initializeEvents() {
        this.analyzeBtn.addEventListener('click', () => this.analyzeCode());
        this.resultDiv.addEventListener('dblclick', () => this.insertSampleCode());
    }

    // Matrix Background Effect
    createMatrixBackground() {
        const canvas = document.createElement('canvas');
        canvas.className = 'matrix-bg';
        document.body.appendChild(canvas);
        const ctx = canvas.getContext('2d');
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
        const chars = '01ABCDEFGHIJKLMNOPQRSTUVWXYZ';
        const charArray = chars.split('');
        const fontSize = 14;
        const columns = canvas.width / fontSize;
        const drops = [];
        for (let i = 0; i < columns; i++) drops[i] = 1;

        function draw() {
            ctx.fillStyle = 'rgba(0, 0, 0, 0.05)';
            ctx.fillRect(0, 0, canvas.width, canvas.height);
            ctx.fillStyle = '#00ff41';
            ctx.font = fontSize + 'px monospace';
            for (let i = 0; i < drops.length; i++) {
                const text = charArray[Math.floor(Math.random() * charArray.length)];
                ctx.fillText(text, i * fontSize, drops[i] * fontSize);
                if (drops[i] * fontSize > canvas.height && Math.random() > 0.975) drops[i] = 0;
                drops[i]++;
            }
        }
        setInterval(draw, 33);
    }

    // --- MAIN ANALYSIS FUNCTION ---
    async analyzeCode() {
        const code = this.codeInput.value.trim();
        const language = this.languageSelect.value;

        if (!code) {
            this.showError('Please enter some code to analyze.');
            return;
        }

        this.showLoading(true);

        try {
            const response = await fetch('/api/analyze', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ code: code, language: language })
            });

            const data = await response.json();

            if (!response.ok) throw new Error(data.error || 'Analysis failed');

            // Call the dynamic block builder
            this.showDynamicBlocks(data.analysis);

        } catch (error) {
            this.showError('Analysis failed: ' + error.message);
        } finally {
            this.showLoading(false);
        }
    }

    // --- DYNAMIC BLOCK BUILDER ---
    // This splits the AI text into separate blocks automatically
    showDynamicBlocks(analysisText) {

        // Regex to find headers like "1. **Title**" or "* **Title**"
        const regex = /(?:^|\n)(?:\d+\.|-|\*)\s+\*\*(.*?)\*\*/g;

        let match;
        const blocks = [];
        let lastIndex = 0;

        // Loop through all matches found by Regex
        while ((match = regex.exec(analysisText)) !== null) {
            // Save content of the PREVIOUS block
            if (blocks.length > 0) {
                const previousBlock = blocks[blocks.length - 1];
                previousBlock.content = analysisText.substring(previousBlock.endIndex, match.index).trim();
            }

            // Create NEW block
            blocks.push({
                title: match[1], // The text inside the **bold** markers
                startIndex: match.index,
                endIndex: match.index + match[0].length,
                content: ""
            });
            lastIndex = match.index + match[0].length;
        }

        // Add the content for the very last block
        if (blocks.length > 0) {
            blocks[blocks.length - 1].content = analysisText.substring(lastIndex).trim();
        }

        // FALLBACK: If AI format wasn't detected (no bold lists), show raw text
        if (blocks.length === 0) {
            this.resultDiv.innerHTML = `
                <div class="vulnerability-blocks">
                    <div class="vulnerability-block vulnerability-info">
                        <div class="vulnerability-header"><div class="vulnerability-title">AI Assessment Report</div></div>
                        <div class="vulnerability-description" style="white-space: pre-wrap;">${analysisText}</div>
                    </div>
                </div>`;
            return;
        }

        // Generate HTML
        let html = '<div class="vulnerability-blocks">';

        blocks.forEach(block => {
            let description = block.content;
            let codeSnippet = "";

            // Check if there is a code block (```code```)
            const codeMatch = description.match(/```([\s\S]*?)```/);
            if (codeMatch) {
                codeSnippet = codeMatch[1]; // Extract code
                description = description.replace(codeMatch[0], ""); // Remove code from text description
            }

            // Determine Severity Color based on keywords in the Title
            let severity = "info"; // Default (Blue/Grey)
            const titleLower = block.title.toLowerCase();

            if (titleLower.includes("vulnerability") || titleLower.includes("injection") || titleLower.includes("critical") || titleLower.includes("overflow") || titleLower.includes("xss")) {
                severity = "critical"; // Red
            } else if (titleLower.includes("warning") || titleLower.includes("risk") || titleLower.includes("issue") || titleLower.includes("security")) {
                severity = "high"; // Orange
            } else if (titleLower.includes("practice") || titleLower.includes("quality") || titleLower.includes("recommendation")) {
                severity = "medium"; // Yellow
            }

            // Build Block HTML
            html += `
                <div class="vulnerability-block vulnerability-${severity}">
                    <div class="vulnerability-header">
                        <div class="vulnerability-title">${block.title}</div>
                        <div class="severity">${severity.toUpperCase()}</div>
                    </div>

                    <div class="vulnerability-description">
                        ${description.replace(/\n/g, '<br>')}
                    </div>

                    ${codeSnippet ? `
                    <div class="vulnerability-solution">
                        <div class="solution-title">💻 Code Example / Fix</div>
                        <div class="code-snippet">${codeSnippet}</div>
                    </div>` : ''}
                </div>
            `;
        });

        html += '</div>';
        this.resultDiv.innerHTML = html;
    }

    showError(message) {
        this.resultDiv.innerHTML = `
            <div class="vulnerability-blocks">
                <div class="vulnerability-block vulnerability-critical">
                    <div class="vulnerability-description">❌ ${message}</div>
                </div>
            </div>
        `;
    }

    showLoading(show) {
        if (show) {
            this.loadingDiv.classList.remove('hidden');
            this.analyzeBtn.disabled = true;
            this.analyzeBtn.textContent = 'SCANNING...';
        } else {
            this.loadingDiv.classList.add('hidden');
            this.analyzeBtn.disabled = false;
            this.analyzeBtn.textContent = '🔍 ANALYZE';
        }
    }

    insertSampleCode() {
        const samples = {
            'Java': `String query = "SELECT * FROM users WHERE name = '" + request.getParameter("name") + "'";`,
            'Python': `import os\nuser_input = input()\nos.system("echo " + user_input)`
        };
        const lang = this.languageSelect.value;
        if (samples[lang]) this.codeInput.value = samples[lang];
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new VulnerabilityAnalyzer();
});