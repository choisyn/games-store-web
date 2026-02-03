// 仪表盘页面
async function loadDashboard() {
    const contentArea = document.getElementById('content-area');
    
    // 获取统计数据
    const userStats = await fetch('/api/admin/users/stats').then(r => r.json());
    const games = await fetch('/api/admin/games').then(r => r.json());
    
    contentArea.innerHTML = `
        <div class="page-header">
            <h2><i class="bi bi-speedometer2"></i> 仪表盘</h2>
        </div>
        
        <div class="stats-grid">
            <div class="stat-card">
                <div class="icon"><i class="bi bi-people"></i></div>
                <h3>总用户数</h3>
                <div class="value">${userStats.data.totalUsers}</div>
                <small>活跃用户: ${userStats.data.activeUsers}</small>
            </div>
            
            <div class="stat-card">
                <div class="icon"><i class="bi bi-controller"></i></div>
                <h3>游戏总数</h3>
                <div class="value">${games.data.length}</div>
                <small>已上架游戏</small>
            </div>
            
            <div class="stat-card">
                <div class="icon"><i class="bi bi-chat-dots"></i></div>
                <h3>社区帖子</h3>
                <div class="value">0</div>
                <small>待审核: 0</small>
            </div>
            
            <div class="stat-card">
                <div class="icon"><i class="bi bi-cash-stack"></i></div>
                <h3>总销售额</h3>
                <div class="value">¥0</div>
                <small>本月收入</small>
            </div>
        </div>
        
        <div class="data-table">
            <div class="p-4">
                <h4 class="mb-3"><i class="bi bi-graph-up"></i> 系统概览</h4>
                <p>欢迎使用游戏社区商城后台管理系统！</p>
                <p>请从左侧菜单选择要管理的功能模块。</p>
            </div>
        </div>
    `;
}

