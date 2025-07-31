// Global variables
let memoryBlocks = [];
let processes = [];
let pageFrames = [];
let isLoading = false;

// Initialize when page loads
document.addEventListener('DOMContentLoaded', function () {
    logActivity('🚀 Frontend initialized');
    initializeSystem();
});

// Initialize all components
async function initializeSystem() {
    try {
        setLoading(true);
        logActivity('🔄 Loading data from backend...');

        await Promise.all([
            loadMemoryBlocks(),
            loadProcesses(),
            loadMemoryStats(),
            updateSwapAndRamLists()
        ]);

        generatePageFrames();
        logActivity('✅ System initialization complete');

        // Auto-refresh every 30 seconds
        setInterval(loadMemoryStats, 30000);

    } catch (error) {
        logActivity(`❌ Initialization failed: ${error.message}`);
    } finally {
        setLoading(false);
    }
}

// Loading state management
function setLoading(loading) {
    isLoading = loading;
    const container = document.querySelector('.container');
    if (loading) {
        container.classList.add('loading');
    } else {
        container.classList.remove('loading');
    }
}

// Memory Allocation Functions
async function allocateMemory() {
    if (isLoading) return;

    const size = parseInt(document.getElementById('memorySize').value);
    const algorithm = document.getElementById('allocationAlgorithm').value;

    try {
        setLoading(true);
        const response = await fetch('/api/allocate', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: `size=${size}&algorithm=${algorithm}`
        });

        if (!response.ok) throw new Error(`HTTP ${response.status}`);

        const result = await response.json();

        if (result.success) {
            logActivity(`✅ Memory allocated: ${size} blocks using ${algorithm} fit`);
            memoryBlocks = result.memoryBlocks;
            updateMemoryVisualization();
            updateStats(result.stats);
        } else {
            logActivity(`❌ Allocation failed: ${result.message}`);
        }

    } catch (error) {
        logActivity(`❌ Allocation error: ${error.message}`);
    } finally {
        setLoading(false);
    }
}

async function deallocateMemory() {
    if (isLoading) return;

    const allocatedBlocks = memoryBlocks.filter(block => !block.free);
    if (!allocatedBlocks.length) {
        logActivity('⚠️ No allocated blocks to deallocate');
        return;
    }

    const randomBlock = allocatedBlocks[Math.floor(Math.random() * allocatedBlocks.length)];

    try {
        setLoading(true);
        const response = await fetch('/api/deallocate', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: `blockId=${randomBlock.id}`
        });

        if (!response.ok) throw new Error(`HTTP ${response.status}`);

        const result = await response.json();

        if (result.success) {
            logActivity(`✅ Memory deallocated: Block ${randomBlock.id}`);
            memoryBlocks = result.memoryBlocks;
            updateMemoryVisualization();
            updateStats(result.stats);
        }

    } catch (error) {
        logActivity(`❌ Deallocation error: ${error.message}`);
    } finally {
        setLoading(false);
    }
}

async function resetMemory() {
    if (isLoading) return;

    try {
        setLoading(true);
        const response = await fetch('/api/reset', {method: 'POST'});

        if (!response.ok) throw new Error(`HTTP ${response.status}`);

        const result = await response.json();

        if (result.success) {
            logActivity('🔄 Memory reset successfully');
            memoryBlocks = result.memoryBlocks;
            updateMemoryVisualization();
            updateStats(result.stats);
        }

    } catch (error) {
        logActivity(`❌ Reset error: ${error.message}`);
    } finally {
        setLoading(false);
    }
}

// Process Management Functions
async function createProcess() {
    if (isLoading) return;

    const name = document.getElementById('processName').value.trim();
    const size = parseInt(document.getElementById('processSize').value);

    if (!name) {
        logActivity('⚠️ Please enter a process name');
        return;
    }

    try {
        setLoading(true);
        const response = await fetch('/api/create-process', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: `name=${encodeURIComponent(name)}&size=${size}`
        });

        if (!response.ok) throw new Error(`HTTP ${response.status}`);

        const result = await response.json();

        if (result.success) {
            logActivity(`✅ Process created: ${name} (Size: ${size})`);
            processes = result.processes;
            updateProcessList();
            document.getElementById('processName').value = '';
            await updateSwapAndRamLists();
        } else {
            logActivity(`❌ Process creation failed: ${result.message}`);
        }

    } catch (error) {
        logActivity(`❌ Process creation error: ${error.message}`);
    } finally {
        setLoading(false);
    }
}

// Swap Management Functions
async function swapOutProcess() {
    if (isLoading) return;

    const processId = document.getElementById("swapProcessId").value.trim();
    if (!processId) {
        alert("Please enter a Process ID.");
        return;
    }

    try {
        setLoading(true);
        const response = await fetch(`/api/swap/out?processId=${encodeURIComponent(processId)}`, {
            method: "POST"
        });

        if (!response.ok) throw new Error(`HTTP ${response.status}`);

        const message = await response.text();
        alert(message);
        logActivity(`⬇️ ${message}`);

        await updateSwapAndRamLists();
        document.getElementById("swapProcessId").value = '';

    } catch (error) {
        const errorMsg = `Backend connection failed: ${error.message}`;
        alert(`❌ ${errorMsg}`);
        logActivity(`❌ Swap out error: ${errorMsg}`);
    } finally {
        setLoading(false);
    }
}

async function swapInProcess() {
    if (isLoading) return;

    const processId = document.getElementById("swapProcessId").value.trim();
    if (!processId) {
        alert("Please enter a Process ID.");
        return;
    }

    try {
        setLoading(true);
        const response = await fetch(`/api/swap/in?processId=${encodeURIComponent(processId)}`, {
            method: "POST"
        });

        if (!response.ok) throw new Error(`HTTP ${response.status}`);

        const message = await response.text();
        alert(message);
        logActivity(`⬆️ ${message}`);

        await updateSwapAndRamLists();
        document.getElementById("swapProcessId").value = '';

    } catch (error) {
        const errorMsg = `Backend connection failed: ${error.message}`;
        alert(`❌ ${errorMsg}`);
        logActivity(`❌ Swap in error: ${errorMsg}`);
    } finally {
        setLoading(false);
    }
}

async function updateSwapAndRamLists() {
    try {
        const [ramResponse, swapResponse] = await Promise.all([
            fetch("/api/swap/ram"),
            fetch("/api/swap/space")
        ]);

        if (!ramResponse.ok || !swapResponse.ok) {
            throw new Error('Failed to fetch swap data');
        }

        const ramProcesses = await ramResponse.json();
        const swapProcesses = await swapResponse.json();

        const swapList = document.getElementById("swapProcesses");
        const ramList = document.getElementById("ramProcesses");

        updateProcessLists(ramProcesses, swapProcesses);
        logActivity("📊 Swap lists updated from backend");

    } catch (error) {
        logActivity(`❌ Failed to update swap lists: ${error.message}`);
        showErrorInLists("Backend connection failed");
    }
}

function updateProcessLists(ramProcesses, swapProcesses) {
    const ramList = document.getElementById("ramProcesses");
    const swapList = document.getElementById("swapProcesses");

    // Update RAM processes
    ramList.innerHTML = "";
    if (ramProcesses.length === 0) {
        ramList.innerHTML = '<li style="color: #999; font-style: italic;">No processes in RAM</li>';
    } else {
        ramProcesses.forEach(processId => {
            const li = document.createElement("li");
            li.innerHTML = `${processId} <span class="status-indicator status-ram">IN RAM</span>`;
            ramList.appendChild(li);
        });
    }

    // Update Swap processes
    swapList.innerHTML = "";
    if (swapProcesses.length === 0) {
        swapList.innerHTML = '<li style="color: #999; font-style: italic;">No processes in swap space</li>';
    } else {
        swapProcesses.forEach(processId => {
            const li = document.createElement("li");
            li.innerHTML = `${processId} <span class="status-indicator status-swap">IN SWAP</span>`;
            swapList.appendChild(li);
        });
    }
}

function showErrorInLists(errorMessage) {
    const ramList = document.getElementById("ramProcesses");
    const swapList = document.getElementById("swapProcesses");

    ramList.innerHTML = `<li class="error-message">⚠️ ${errorMessage}</li>`;
    swapList.innerHTML = `<li class="error-message">⚠️ ${errorMessage}</li>`;
}

// Page Replacement Functions
async function simulatePageReplacement() {
    if (isLoading) return;

    const algorithm = document.getElementById('pageAlgorithm').value;

    try {
        setLoading(true);
        const response = await fetch('/api/page-replacement', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: `algorithm=${algorithm}`
        });

        if (!response.ok) throw new Error(`HTTP ${response.status}`);

        const result = await response.json();

        logActivity(`📄 Page replacement: ${algorithm.toUpperCase()} - Faults: ${result.pageFaults}, Hit Ratio: ${result.hitRatio}%`);

        pageFrames = result.pageFrames;
        updatePageFrames();
        updateAlgorithmResults(result);

    } catch (error) {
        logActivity(`❌ Page replacement error: ${error.message}`);
    } finally {
        setLoading(false);
    }
}

// Garbage Collection
async function runGarbageCollection() {
    if (isLoading) return;

    try {
        setLoading(true);
        const response = await fetch('/api/garbage-collect', {method: 'POST'});

        if (!response.ok) throw new Error(`HTTP ${response.status}`);

        const result = await response.json();

        if (result.success) {
            logActivity(`🗑️ Garbage collection: ${result.objectsCollected} objects collected`);
            memoryBlocks = result.memoryBlocks;
            updateMemoryVisualization();
            updateStats(result.stats);
        }

    } catch (error) {
        logActivity(`❌ Garbage collection error: ${error.message}`);
    } finally {
        setLoading(false);
    }
}

// Data Loading Functions
async function loadMemoryBlocks() {
    try {
        const response = await fetch('/api/memory-blocks');
        if (!response.ok) throw new Error(`HTTP ${response.status}`);

        memoryBlocks = await response.json();
        updateMemoryVisualization();
        logActivity('📦 Memory blocks loaded');

    } catch (error) {
        logActivity(`❌ Failed to load memory blocks: ${error.message}`);
    }
}

async function loadProcesses() {
    try {
        const response = await fetch('/api/processes');
        if (!response.ok) throw new Error(`HTTP ${response.status}`);

        processes = await response.json();
        updateProcessList();
        logActivity('⚙️ Processes loaded');

    } catch (error) {
        logActivity(`❌ Failed to load processes: ${error.message}`);
    }
}

async function loadMemoryStats() {
    try {
        const response = await fetch('/api/memory-stats');
        if (!response.ok) throw new Error(`HTTP ${response.status}`);

        const stats = await response.json();
        updateStats(stats);

    } catch (error) {
        logActivity(`❌ Failed to load stats: ${error.message}`);
    }
}

// UI Update Functions
function updateMemoryVisualization() {
    const container = document.getElementById('memoryVisualization');
    container.innerHTML = '';

    memoryBlocks.forEach(block => {
        const blockElement = document.createElement('div');
        blockElement.className = `memory-block ${block.free ? 'free' : 'allocated'}`;
        blockElement.title = `Block ${block.id}: ${block.free ? 'Free' : 'Allocated'} (Size: ${block.size || 1})`;
        blockElement.onclick = () => !block.free && deallocateSpecificBlock(block.id);
        container.appendChild(blockElement);
    });
}

function updateStats(stats) {
    const container = document.getElementById('statsGrid');
    container.innerHTML = `
        <div class="stat-card"><h3>${stats.allocatedBlocks || 0}</h3><p>Allocated Blocks</p></div>
        <div class="stat-card"><h3>${stats.freeBlocks || 0}</h3><p>Free Blocks</p></div>
        <div class="stat-card"><h3>${stats.utilizationPercentage || 0}%</h3><p>Memory Usage</p></div>
        <div class="stat-card"><h3>${stats.successRate || 0}%</h3><p>Success Rate</p></div>
        <div class="stat-card"><h3>${stats.activeProcesses || 0}</h3><p>Active Processes</p></div>
        <div class="stat-card"><h3>${stats.gcCollections || 0}</h3><p>GC Collections</p></div>
    `;
}

function updateProcessList() {
    const container = document.getElementById('processList');
    if (processes.length === 0) {
        container.innerHTML = '<p>No processes created yet.</p>';
        return;
    }

    container.innerHTML = '';
    processes.forEach(process => {
        const item = document.createElement('div');
        item.className = 'process-item';
        item.innerHTML = `
            <strong>${process.name}</strong> (ID: ${process.id})<br>
            Size: ${process.size} | Status: ${process.status || 'Unknown'} | Priority: ${process.priority || 'Normal'}
        `;
        container.appendChild(item);
    });
}

function generatePageFrames() {
    pageFrames = Array.from({length: 4}, (_, i) => ({
        frameId: i,
        pageNumber: -1,
        occupied: false
    }));
    updatePageFrames();
}

function updatePageFrames() {
    const container = document.getElementById('pageFrames');
    container.innerHTML = '';

    pageFrames.forEach(frame => {
        const div = document.createElement('div');
        div.className = `page-frame ${frame.occupied ? 'occupied' : ''}`;
        div.innerHTML = frame.pageNumber === -1 ? '-' : frame.pageNumber;
        div.title = `Frame ${frame.frameId}: ${frame.occupied ? `Page ${frame.pageNumber}` : 'Empty'}`;
        container.appendChild(div);
    });
}

function updateAlgorithmResults(result) {
    const container = document.getElementById('algorithmResults');
    container.innerHTML = `
        <h3>Algorithm: ${result.algorithm.toUpperCase()}</h3>
        <p><strong>Page Faults:</strong> ${result.pageFaults}</p>
        <p><strong>Page Hits:</strong> ${result.pageHits}</p>
        <p><strong>Hit Ratio:</strong> ${result.hitRatio}%</p>
        <p><strong>Reference String:</strong> ${result.referenceString || '7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2, 1, 2, 0, 1, 7, 0, 1'}</p>
    `;
}

async function deallocateSpecificBlock(blockId) {
    if (isLoading) return;

    try {
        setLoading(true);
        const response = await fetch('/api/deallocate', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: `blockId=${blockId}`
        });

        if (!response.ok) throw new Error(`HTTP ${response.status}`);

        const result = await response.json();

        if (result.success) {
            logActivity(`✅ Block ${blockId} deallocated`);
            memoryBlocks = result.memoryBlocks;
            updateMemoryVisualization();
            updateStats(result.stats);
        }

    } catch (error) {
        logActivity(`❌ Block deallocation error: ${error.message}`);
    } finally {
        setLoading(false);
    }
}

function logActivity(message) {
    const logArea = document.getElementById('activityLog');
    const timestamp = new Date().toLocaleTimeString();
    const logEntry = document.createElement('div');
    logEntry.innerHTML = `[${timestamp}] ${message}`;
    logArea.appendChild(logEntry);
    logArea.scrollTop = logArea.scrollHeight;

    // Keep only last 50 log entries
    while (logArea.children.length > 50) {
        logArea.removeChild(logArea.firstChild);
    }
}

// Keyboard shortcuts
document.addEventListener('keydown', function (event) {
    if (event.ctrlKey || event.metaKey) {
        switch (event.key.toLowerCase()) {
            case 'r':
                event.preventDefault();
                resetMemory();
                break;
            case 'm':
                event.preventDefault();
                allocateMemory();
                break;
            case 'g':
                event.preventDefault();
                runGarbageCollection();
                break;
        }
    }
});

// Add Enter key support for swap operations
document.getElementById('swapProcessId').addEventListener('keypress', function (event) {
    if (event.key === 'Enter') {
        if (event.shiftKey) {
            swapInProcess();
        } else {
            swapOutProcess();
        }
    }
});

// Add Enter key support for process creation
document.getElementById('processName').addEventListener('keypress', function (event) {
    if (event.key === 'Enter') {
        createProcess();
    }
});

// Connection health check
async function checkBackendConnection() {
    try {
        const response = await fetch('/api/memory-stats', {
            method: 'GET',
            timeout: 5000
        });

        if (response.ok) {
            logActivity('🟢 Backend connection: Healthy');
            return true;
        } else {
            logActivity('🟡 Backend connection: Issues detected');
            return false;
        }
    } catch (error) {
        logActivity('🔴 Backend connection: Failed');
        return false;
    }
}

// Periodic health check
setInterval(checkBackendConnection, 60000); // Check every minute

// Error handling for network issues
window.addEventListener('online', function () {
    logActivity('🌐 Network connection restored');
    initializeSystem();
});

window.addEventListener('offline', function () {
    logActivity('🌐 Network connection lost');
});

// Cleanup on page unload
window.addEventListener('beforeunload', function () {
    logActivity('🔄 Application shutting down...');
});