package com.example.snake.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * 蛇核心数据结构与运动逻辑。
 * <p>
 * 蛇头沿轨迹路径平滑移动，蛇身各节沿轨迹等间距排列。
 * 轨迹点序列（trackPoints）记录蛇头运动的完整路径，
 * 蛇身节点（nodes）通过沿轨迹累计距离来定位。
 * <p>
 * 移动方向使用单位向量 (dirX, dirY) 表示，支持任意角度自由移动。
 */
public class Snake {

    // ================================================================
    // 核心数据结构
    // ================================================================

    /** 蛇头轨迹点序列，索引 0 为最新位置（蛇头），依次向后为历史位置 */
    private final List<float[]> trackPoints;

    /** 蛇身节点列表，索引 0 为蛇头，依次向后为蛇身 */
    private final List<SnakeNode> nodes;

    /** 当前移动方向单位向量（本帧已生效） */
    private float dirX;
    private float dirY;

    /** 待生效方向单位向量（避免同帧多次转向，在下次 move 时生效） */
    private float pendingDirX;
    private float pendingDirY;

    /** 是否有待生效的方向变更 */
    private boolean directionPending;

    /** 节点半径（像素） */
    private final float nodeRadius;

    /** 节点间距（= 节点直径 = nodeRadius * 2） */
    private final float nodeSpacing;

    /** 期望的节点总数（含蛇头），增长时递增 */
    private int expectedNodeCount;

    /** 生长标记，下次 move 时不裁剪尾部并增加一个节点 */
    private boolean growPending;

    // ================================================================
    // 构造与初始化
    // ================================================================

    /**
     * 构造蛇，初始化 3 节，朝右，位于指定起点。
     *
     * @param startX     蛇头初始 X 坐标
     * @param startY     蛇头初始 Y 坐标
     * @param nodeRadius 节点半径（像素）
     */
    public Snake(float startX, float startY, float nodeRadius) {
        this.nodeRadius = nodeRadius;
        this.nodeSpacing = nodeRadius * 2;
        this.trackPoints = new ArrayList<>();
        this.nodes = new ArrayList<>();
        init(startX, startY);
    }

    /**
     * 初始化/重置蛇到指定起点。
     * 初始状态：3 节，朝右，蛇头位于 (startX, startY)，蛇身向左延伸。
     */
    private void init(float startX, float startY) {
        trackPoints.clear();
        nodes.clear();

        // 初始方向：朝右（单位向量）
        dirX = 1f;
        dirY = 0f;
        pendingDirX = 1f;
        pendingDirY = 0f;
        directionPending = false;

        expectedNodeCount = 3;
        growPending = false;

        // 构建初始轨迹：蛇头位置和尾部位置，形成一条水平线段
        float totalDistance = (expectedNodeCount - 1) * nodeSpacing;
        trackPoints.add(new float[]{startX, startY});
        trackPoints.add(new float[]{startX - totalDistance, startY});

        rebuildNodes();
    }

    // ================================================================
    // 方向控制（矢量化）
    // ================================================================

    /**
     * 设置待生效移动方向。
     * 接收原始方向向量（如滑动位移 dx/dy），内部归一化为单位向量。
     * 若新方向与当前有效方向的夹角接近 180°（点积 < -0.9），则忽略（防止逆行自撞）。
     *
     * @param dx 方向 X 分量（如手指滑动水平位移，右为正）
     * @param dy 方向 Y 分量（如手指滑动垂直位移，下为正）
     */
    public void setDirection(float dx, float dy) {
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 1e-6f) return;

        // 归一化
        float ndx = dx / len;
        float ndy = dy / len;

        // 取当前有效方向（待生效方向优先）
        float effX = directionPending ? pendingDirX : dirX;
        float effY = directionPending ? pendingDirY : dirY;

        // 点积 < -0.9 约等于夹角 > 154°，视为逆行，拒绝
        float dot = effX * ndx + effY * ndy;
        if (dot < -0.9f) return;

        pendingDirX = ndx;
        pendingDirY = ndy;
        directionPending = true;
    }

    // ================================================================
    // 移动逻辑
    // ================================================================

    /**
     * 蛇头沿当前方向移动指定距离，更新轨迹和蛇身节点。
     *
     * @param distance 本帧移动距离（像素）
     */
    public void move(float distance) {
        if (distance <= 0) return;

        // 0. 立即生效待定方向（当前帧生效）
        if (directionPending) {
            dirX = pendingDirX;
            dirY = pendingDirY;
            directionPending = false;
        }

        // 1. 蛇头沿方向向量移动(新位置 = 旧蛇头位置 + 方向 × 距离)
        float[] headPos = trackPoints.get(0);
        float newX = headPos[0] + dirX * distance;
        float newY = headPos[1] + dirY * distance;
        // 把新位置插入到轨迹最前面
        trackPoints.add(0, new float[]{newX, newY});

        // 2. 处理生长
        if (growPending) {
            expectedNodeCount++;
            growPending = false;
        }

        // 3. 重建节点 & 裁剪轨迹
        rebuildNodes();
        trimTrackPoints();
    }

    /**
     * 标记需要生长。下次 move() 时不裁剪尾部，实际增加一个节点。
     */
    public void grow() {
        growPending = true;
    }

    // ================================================================
    // 节点重建（核心算法）
    // ================================================================

    private void rebuildNodes() {
        ensureNodeCount();

        if (trackPoints.isEmpty() || expectedNodeCount <= 0) {
            return;
        }

        // 第1步：蛇头直接放轨迹起点
        placeHeadNode();

        // 第2步：沿轨迹等间距放置身节点
        if (expectedNodeCount > 1) {
            placeBodyNodesAlongTrack();
        }
    }

// ========== 子方法 ==========

    /**
     * 确保 nodes 列表大小 == expectedNodeCount
     */
    private void ensureNodeCount() {
        // 不够就补（新节点暂放尾部位置，后面会重算）
        while (nodes.size() < expectedNodeCount) {
            if (nodes.isEmpty()) {
                nodes.add(new SnakeNode(0, 0, nodeRadius));
            } else {
                SnakeNode tail = nodes.get(nodes.size() - 1);
                nodes.add(new SnakeNode(tail.x, tail.y, nodeRadius));
            }
        }
        // 多了就删
        while (nodes.size() > expectedNodeCount) {
            nodes.remove(nodes.size() - 1);
        }
    }

    /**
     * 蛇头 = 轨迹最新点
     */
    private void placeHeadNode() {
        float[] headPoint = trackPoints.get(0);
        SnakeNode head = nodes.get(0);
        head.x = headPoint[0];
        head.y = headPoint[1];
    }

    /**
     * 核心算法：沿折线轨迹，从蛇头出发，每隔 nodeSpacing 放一个身节点。
     *
     */
    private void placeBodyNodesAlongTrack() {
        // 需要放的身节点：从 index=1 到末尾
        int nextNodeIdx = 1;              // 下一个要放置的节点索引
        float distRemaining = nodeSpacing; // 到下一个节点还需走多少像素

        int segIdx = 0;       // 当前看第几段线段
        float segUsed = 0;    // 当前线段已经"消耗"了多少长度

        while (nextNodeIdx < expectedNodeCount
                && segIdx < trackPoints.size() - 1) {

            // 当前线段：从 trackPoints[segIdx] 到 trackPoints[segIdx+1]
            float[] p1 = trackPoints.get(segIdx);
            float[] p2 = trackPoints.get(segIdx + 1);
            float segLen = distance(p1, p2);

            // 长度为0的退化线段，跳过
            if (segLen <= 0) {
                segIdx++;
                segUsed = 0;
                continue;
            }

            float segLeft = segLen - segUsed; // 当前线段还剩多少

            if (distRemaining <= segLeft) {
                // 当前线段剩余长度足够 → 节点就落在这里
                placeNodeOnSegment(nextNodeIdx, p1, p2, segLen, segUsed + distRemaining);

                segUsed += distRemaining;
                nextNodeIdx++;
                distRemaining = nodeSpacing;  // 重置：下一个节点又隔 nodeSpacing
            } else {
                // 不够 → 走完这段，剩下的距离带到下一段
                distRemaining -= segLeft;
                segIdx++;
                segUsed = 0;
            }
        }
        // 兜底：轨迹太短放不下的节点，全部叠在前一个节点上
        while (nextNodeIdx < nodes.size()) {
            SnakeNode prev = nodes.get(nextNodeIdx - 1);
            SnakeNode curr = nodes.get(nextNodeIdx);
            curr.x = prev.x;
            curr.y = prev.y;
            nextNodeIdx++;
        }
    }

    /**
     * 在线段 p1→p2 上，从 p1 出发走过 totalOffset 像素的位置，
     * 放置 nodes[nodeIdx]。
     *
     * 原理：线性插值
     *   t = totalOffset / 线段总长
     *   位置 = p1 + (p2 - p1) × t
     */
    private void placeNodeOnSegment(int nodeIdx, float[] p1, float[] p2,
                                    float segLen, float totalOffset) {
        float t = totalOffset / segLen;
        SnakeNode node = nodes.get(nodeIdx);
        node.x = p1[0] + (p2[0] - p1[0]) * t;
        node.y = p1[1] + (p2[1] - p1[1]) * t;
    }

    /** 计算两点间距离 */
    private float distance(float[] a, float[] b) {
        float dx = b[0] - a[0];
        float dy = b[1] - a[1];
        return (float) Math.sqrt(dx * dx + dy * dy);
    }


    private void trimTrackPoints() {           // 裁剪多余历史轨迹，防止内存无限增长
        float neededDistance = (expectedNodeCount - 1) * nodeSpacing; // 计算蛇身所需的总轨迹长度
        float accumulated = 0;                // 累计已遍历的轨迹长度
        int lastNeededIndex = 0;                // 记录满足长度需求的最后一个轨迹点索引

        // 从蛇头向后遍历轨迹线段，累加长度直到满足蛇身需求
        for (int i = 0; i < trackPoints.size() - 1; i++) {
            float[] p1 = trackPoints.get(i);          // 当前线段起点
            float[] p2 = trackPoints.get(i + 1);       // 当前线段终点
            float dx = p2[0] - p1[0];                  // X 差值
            float dy = p2[1] - p1[1];                  // Y 差值
            float len = (float) Math.sqrt(dx * dx + dy * dy); // 线段长度
            accumulated += len;                         // 累加到总长度
            lastNeededIndex = i + 1;                    // 更新最后需要的点索引
            if (accumulated >= neededDistance) {        // 若已满足蛇身长度需求
                break;                                  // 停止遍历
            }
        }

        // 删除 lastNeededIndex 之后的所有多余轨迹点
        while (trackPoints.size() > lastNeededIndex + 1) { // 若列表长度超过所需
            trackPoints.remove(trackPoints.size() - 1);   // 从尾部移除旧轨迹点
        }
    }

    // ================================================================
    // 穿墙模式支持
    // ================================================================

    public void applyWrap(float dx, float dy) {
        for (float[] p : trackPoints) {
            p[0] += dx;
            p[1] += dy;
        }
        for (SnakeNode n : nodes) {
            n.x += dx;
            n.y += dy;
        }
    }

    // ================================================================
    // 查询方法
    // ================================================================

    public SnakeNode getHead() {
        return nodes.get(0);
    }

    public List<SnakeNode> getBodyForCollisionCheck() {
        int skip = Math.min(3, nodes.size());
        return nodes.subList(skip, nodes.size());
    }

    public List<SnakeNode> getNodes() {
        return nodes;
    }


    // ================================================================
    // 重置
    // ================================================================

    public void reset(float startX, float startY) {
        init(startX, startY);
    }
}