// 社区管理页面
let currentPosts = [];

async function loadCommunityPage() {
    const contentArea = document.getElementById('content-area');
    
    contentArea.innerHTML = `
        <div class="page-header">
            <h2><i class="bi bi-chat-square-dots"></i> 社区管理</h2>
        </div>
        
        <ul class="nav nav-tabs mb-3" id="communityTabs">
            <li class="nav-item">
                <a class="nav-link active" data-bs-toggle="tab" href="#posts-tab">帖子管理</a>
            </li>
            <li class="nav-item">
                <a class="nav-link" data-bs-toggle="tab" href="#comments-tab">评论管理</a>
            </li>
        </ul>
        
        <div class="tab-content">
            <!-- 帖子管理 -->
            <div class="tab-pane fade show active" id="posts-tab">
                <div class="data-table">
                    <table class="table table-hover">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>标题</th>
                                <th>作者ID</th>
                                <th>板块ID</th>
                                <th>浏览数</th>
                                <th>点赞数</th>
                                <th>评论数</th>
                                <th>状态</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody id="postsTableBody">
                            <tr><td colspan="9" class="text-center">加载中...</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>
            
            <!-- 评论管理 -->
            <div class="tab-pane fade" id="comments-tab">
                <div class="data-table">
                    <table class="table table-hover">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>内容</th>
                                <th>用户ID</th>
                                <th>帖子ID</th>
                                <th>发布时间</th>
                                <th>状态</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody id="commentsTableBody">
                            <tr><td colspan="7" class="text-center">暂无评论数据</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        
        <!-- 帖子详情模态框 -->
        <div class="modal fade" id="postDetailModal" tabindex="-1">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">帖子详情</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body" id="postDetailContent">
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    loadPosts();
}

async function loadPosts() {
    try {
        const response = await fetch('/api/admin/community/posts');
        const data = await response.json();
        
        if (data.code === 200) {
            currentPosts = data.data;
            displayPosts(data.data);
        }
    } catch (error) {
        console.error('加载帖子失败:', error);
        document.getElementById('postsTableBody').innerHTML = 
            '<tr><td colspan="9" class="text-center">加载失败</td></tr>';
    }
}

function displayPosts(posts) {
    const tbody = document.getElementById('postsTableBody');
    
    if (posts.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="text-center">暂无帖子</td></tr>';
        return;
    }
    
    tbody.innerHTML = posts.map(post => `
        <tr>
            <td>${post.id}</td>
            <td><a href="#" onclick="viewPostDetail(${post.id}); return false;">${post.title}</a></td>
            <td>${post.userId}</td>
            <td>${post.sectionId}</td>
            <td>${post.viewCount || 0}</td>
            <td>${post.likeCount || 0}</td>
            <td>${post.commentCount || 0}</td>
            <td>
                <span class="badge bg-${post.status === 'PUBLISHED' ? 'success' : 'secondary'}">
                    ${post.status}
                </span>
                ${post.isPinned ? '<span class="badge bg-warning">置顶</span>' : ''}
                ${post.isEssence ? '<span class="badge bg-info">精华</span>' : ''}
            </td>
            <td>
                <div class="btn-group btn-group-sm">
                    <button class="btn btn-warning" onclick="togglePin(${post.id}, ${!post.isPinned})" title="${post.isPinned ? '取消置顶' : '置顶'}">
                        <i class="bi bi-pin${post.isPinned ? '-fill' : ''}"></i>
                    </button>
                    <button class="btn btn-info" onclick="toggleEssence(${post.id}, ${!post.isEssence})" title="${post.isEssence ? '取消精华' : '设为精华'}">
                        <i class="bi bi-star${post.isEssence ? '-fill' : ''}"></i>
                    </button>
                    <button class="btn btn-secondary" onclick="toggleClose(${post.id}, ${!post.isClosed})" title="${post.isClosed ? '开启评论' : '关闭评论'}">
                        <i class="bi bi-lock${post.isClosed ? '-fill' : ''}"></i>
                    </button>
                    <button class="btn btn-danger" onclick="deletePost(${post.id})" title="删除">
                        <i class="bi bi-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

async function viewPostDetail(postId) {
    try {
        const response = await fetch(`/api/admin/community/posts/${postId}`);
        const data = await response.json();
        
        if (data.code === 200) {
            const post = data.data;
            document.getElementById('postDetailContent').innerHTML = `
                <h5>${post.title}</h5>
                <p class="text-muted">作者ID: ${post.userId} | 发布时间: ${formatDate(post.createdAt)}</p>
                <hr>
                <div>${post.content}</div>
                ${post.imageUrl ? `<img src="${post.imageUrl}" class="img-fluid mt-3">` : ''}
            `;
            
            const modal = new bootstrap.Modal(document.getElementById('postDetailModal'));
            modal.show();
        }
    } catch (error) {
        console.error('加载帖子详情失败:', error);
    }
}

async function togglePin(postId, isPinned) {
    try {
        const response = await fetch(`/api/admin/community/posts/${postId}/pin?isPinned=${isPinned}`, {
            method: 'PUT'
        });
        
        const data = await response.json();
        
        if (data.code === 200) {
            alert(isPinned ? '置顶成功！' : '取消置顶成功！');
            loadPosts();
        }
    } catch (error) {
        console.error('操作失败:', error);
    }
}

async function toggleEssence(postId, isEssence) {
    try {
        const response = await fetch(`/api/admin/community/posts/${postId}/essence?isEssence=${isEssence}`, {
            method: 'PUT'
        });
        
        const data = await response.json();
        
        if (data.code === 200) {
            alert(isEssence ? '设为精华成功！' : '取消精华成功！');
            loadPosts();
        }
    } catch (error) {
        console.error('操作失败:', error);
    }
}

async function toggleClose(postId, isClosed) {
    try {
        const response = await fetch(`/api/admin/community/posts/${postId}/close?isClosed=${isClosed}`, {
            method: 'PUT'
        });
        
        const data = await response.json();
        
        if (data.code === 200) {
            alert(isClosed ? '关闭评论成功！' : '开启评论成功！');
            loadPosts();
        }
    } catch (error) {
        console.error('操作失败:', error);
    }
}

async function deletePost(postId) {
    if (!confirm('确定要删除这个帖子吗？')) return;
    
    try {
        const response = await fetch(`/api/admin/community/posts/${postId}`, {
            method: 'DELETE'
        });
        
        const data = await response.json();
        
        if (data.code === 200) {
            alert('删除成功！');
            loadPosts();
        }
    } catch (error) {
        console.error('删除失败:', error);
    }
}

function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleString('zh-CN');
}

