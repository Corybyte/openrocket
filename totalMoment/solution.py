import math


def calculateMoment(instanceList, cnaList, cpList, aoa, refLength, nextDouble, finSetFlags, PitchDampingMoment,
                    YawDampingMoment, centerOfMass, tubeFinSetFlags, cRollDamps, cRollForces):
    # 用于随机扰动的常数
    PITCH_YAW_RANDOM = 0.0005
    totalCM = 0  # 总俯仰力矩
    totalCN = 0  # 总法向力
    totalCyaw = 0  # 总偏航力矩
    totalCside = 0  # 总侧向力
    totalCRoll = 0  # 总滚转力矩

    ############### Begin #############
    for i in range(len(instanceList)):
        # 计算当前组件的力矩
        componentCM, componentCN, componentCRoll = calculateComponentNonAxialForces(instanceList[i], cnaList[i],
                                                                                    cpList[i], aoa,
                                                                                    refLength, finSetFlags[i],
                                                                                    cRollForces[i],
                                                                                    cRollDamps[i], tubeFinSetFlags[i])
        # 累加各组件的力矩
        totalCM += componentCM
        totalCN += componentCN
        totalCRoll += componentCRoll
    # 减去俯仰阻尼力矩和偏航阻尼力矩
    totalCM -= PitchDampingMoment
    totalCyaw -= YawDampingMoment
    # 添加随机扰动（模拟一些不可预测的变化）
    totalCyaw = totalCyaw + (PITCH_YAW_RANDOM * 2 * (nextDouble - 0.5))
    totalCM = totalCM + (PITCH_YAW_RANDOM * 2 * (nextDouble - 0.5))

    # 计算总的力矩，并根据质心位置进行修正
    cm = totalCM - totalCN * centerOfMass.x / refLength
    cyaw = totalCyaw - totalCside * centerOfMass.x / refLength

    # 返回计算得到的力矩值
    return cm, cyaw, totalCRoll
    ############### End #############


# 计算当前组件所有实例的cm
def calculateComponentNonAxialForces(contextNum, instancesCNas, instancesCPs, aoa, refLength, isFinset, cRollForces,
                                     CRollDamps, isTubeFinSet):
    ############### Begin #############
    instancesCM = 0  # 各实例的力矩
    instancesCN = 0  # 各实例的法向力
    instancesCroll = 0  # 各实例的滚转力矩
    STALL_ANGLE = (20 * math.pi / 180)  # 陀螺失速角度

    for i in range(contextNum):
        if isFinset[i]:
            # 如果是鳍片组件，根据攻角限制计算法向力
            CN_instanced = instancesCNas[i] * min(aoa, STALL_ANGLE)
        else:
            CN_instanced = instancesCNas[i] * aoa

        # 计算滚转力
        CRoll_instanced = cRollForces[i] - CRollDamps[i]
        # 计算气动中心位置
        CP_instanced = instancesCPs[i]

        # 计算单个实例的力矩
        instanceCM = CN_instanced * CP_instanced / refLength

        # 累加各实例的力矩、法向力和滚转力矩
        instancesCM += instanceCM
        instancesCN += CN_instanced
        instancesCroll += CRoll_instanced

    # 返回所有实例的力矩、法向力和滚转力矩
    return instancesCM, instancesCN, instancesCroll
    ############### End #############
