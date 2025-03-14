import math

from flask import Flask, request, jsonify

from calculateAcceleration.pltImages import plot_rocket_from_json
from calculateCD.calculateCDHelper import calculateCD
from calculateAcceleration.calculateAccelerationHelper import calculateAccelerationHelper
from calculateAxialCD.calculateAxialCDHelper import calculateAixalCDHelper
from calculateFinsetPCD.calculateFinsetPressureCDHelper import calculateFSPCD
from calculateFrictionCD.calculateFrictionCDHelper import calculateFriectionCDHelper
from calculateSymComponentPCD import calculateSymPCDHelper
from calculateStability.calculateStabilityHelper import calculateStabilityHelper
from calculateTotalPressureCD.totalPressureCDHelper import calculatePressureCDHelper
from calculateGlideDistance.calculateGlideDistanceHelper import calculateGlideDistancehelper
from calculateGlideCharacter.calculateGlideCharacterHelper import calculateGlideCharacterhelper
from calculatecomponentNonAxialForces import addition_CN
from calculateAcceleration.pltImage2 import calculateAccelerationPLT
from calculateAxialCD.pltImage import axialCDPLT
from calculateCD.pltImage import cdPLT
from calculateFinsetPCD.pltImage import finsetPressureCDPLT
from calculateFrictionCD.pltImage import frictionPLT
from calculateStability.pltImage import stabilityPLT
from calculateSymComponentPCD.pltImage import pressureCD
from calculateTotalPressureCD.pltImage import totalpressureCD
from calculateTubeFinSetHullCG.pltImage import hullCNPLT
from calculatecomponentNonAxialForces.pltImage import componentNonAxialForcePLT
from totalMoment.pltImage import totalMomentPLTImage
from wingDemo.pltImage import WingCNPLT
from totalMoment.totalMomentHelper import calculateTotalMomentHelper
from utils import *
from podsCG import pods_cg_helper
from podsCP import pods_cp_helper
from podsMOI import pods_moi_helper
from bodyTubeMOI import body_tube_moi_helper
from innerComponentCG import inner_component_cg_helper
from innerComponentMOI import inner_component_moi_helper
from innerTubeCG import inner_tube_cg_helper
from innerTubeMOI import inner_tube_moi_helper
from launchLugCG import launchlug_cg_helper
from launchLugCP import launchlug_cp_helper
from launchLugMOI import launchlug_moi_helper
from massComponentCG import mass_component_cg_helper
from massComponentMOI import mass_component_moi_helper
from parachuteCG import parachute_cg_helper
from parachuteMOI import parachute_moi_helper
from railButtonCG import rail_button_cg_helper
from railButtonCP import rail_button_cp_helper
from railButtonMOI import rail_button_moi_helper
from shockCordCG import shockCord_cg_helper
from shockCordMOI import shockCord_moi_helper
from stageCG import stage_cg_helper
from stageCP import stage_cp_helper
from stageMOI import stage_moi_helper
from streamerCG import streamer_cg_helper
from streamerMOI import streamer_moi_helper
from transitionCG import transition_cg_helper
from transitionCP import transition_cp_helper
from transitionMOI import transition_moi_helper
from tubeFinsetCG import tube_fine_set_cg_helper
from tubeFinsetCP import tube_fine_set_cp_helper
from tubeFinsetMOI import tube_fine_set_moi_helper
from wholeCG import whole_cg_helper
from wholeCP import whole_cp_helper
from wholeMOI import whole_moi_helper
from motorFunction import myFunction
from motorPoint import myPoint
from utils.common_helper import equals
import os
from noseConeCG import nose_cone_cg_helper
from noseConeCP import nose_cone_cp_helper
from noseConeMOI import nose_cone_moi_helper
from finSetCG import finset_cg_helper
from finSetCP import finset_cp_helper
from finSetMOI import finset_moi_helper
from bodyTubeCG import body_tube_cg_helper
from bodyTubeCP import body_tube_cp_helper

import traceback
from calculateTubeFinSetHullCG import demo
from wingDemo import calculate_nonaxial_forces

app = Flask(__name__)


#
# # 禁用 Flask 默认日志
# log = logging.getLogger('werkzeug')
# log.setLevel(logging.ERROR)  # 只显示 ERROR 级别的日志

# def check(result, answer, dir):
#     if equals(result, answer):
#         with open(os.path.join(dir, "result.txt"), 'w') as f:
#             f.write("Yes")
#     else:
#         with open(os.path.join(dir, "result.txt"), 'w') as f:
#             f.write("No")

# 适用于组件对比
def check(result, answer, dir):
    # 判断是否为元组
    if isinstance(result, list):
        flag = True
        flag2 = True
        if not equals(round(result[0], 5), round(answer[0], 5)):
            flag = False
        if not equals(round(result[1], 5), round(answer[1], 5)):
            flag2 = False
        if flag and flag2:
            with open(os.path.join(dir, "result.txt"), 'w') as f:
                f.write("头歌实践教学平台欢迎你\n请认真作答\nYes")
        elif flag and not flag2:
            with open(os.path.join(dir, "result.txt"), 'w') as f:
                f.write(" longitudinalUnitInertia 计算错误")
                relative_error = abs(result[1] - answer[1])
                f.write(f"n（误差：{relative_error:.2f}%）\n")
        elif not flag and flag2:
            with open(os.path.join(dir, "result.txt"), 'w') as f:
                f.write(" rotationalUnitInertia 计算错误")
                relative_error = abs(result[0] - answer[0])
                f.write(f"n（误差：{relative_error:.2f}%）\n")

        else:
            with open(os.path.join(dir, "result.txt"), 'w') as f:
                f.write("rotationalUnitInertia and longitudinalUnitInertia Error")
    else:
        if equals(round(result, 5), round(answer, 5)):
            with open(os.path.join(dir, "result.txt"), 'w') as f:
                f.write("头歌实践教学平台欢迎你\n请认真作答\nYes")
        else:

            relative_error = abs(result - answer)
            print(relative_error)
            with open(os.path.join(dir, "result.txt"), 'w') as f:
                f.write("头歌实践教学平台欢迎你\n请认真作答\n")
                f.write(f"答案错误\n（误差：{relative_error:.2f}%）\n")


# NoseCone
@app.route('/NoseCone/calculateCG', methods=['POST'])
def calculateNoseConeCG():
    app.logger.info(f"{request.json}")
    try:
        cg = nose_cone_cg_helper.calculateNoseConeCG(request.json)
        error_file_path = "/data/workspace/myshixun/noseConeCG/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cg, request.json['answer'], os.path.join(os.getcwd(), "noseConeCG"))
        return {"code": 200, "msg": "ok", "result": cg}
    except Exception:
        with open(os.path.join("noseConeCG", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/NoseCone/calculateCP', methods=['POST'])
def calculateNoseConeCP():
    app.logger.info(f"{request.json}")
    try:
        cp = nose_cone_cp_helper.calculateNoseConeCP(request.json)
        error_file_path = "/data/workspace/myshixun/noseConeCP/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cp, request.json['answer'], os.path.join(os.getcwd(), "noseConeCP"))
        return {"code": 200, "msg": "ok", "result": cp}
    except Exception:
        with open(os.path.join("noseConeCP", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/NoseCone/calculateMOI', methods=['POST'])
def calculateNoseConeMOI():
    app.logger.info(f"{request.json}")
    try:
        moi = nose_cone_moi_helper.calculateMOI(request.json)
        error_file_path = "/data/workspace/myshixun/noseConeMOI/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(moi, request.json['answer'], os.path.join(os.getcwd(), "noseConeMOI"))
        return {"code": 200, "msg": "ok", "result": moi}
    except Exception:
        with open(os.path.join("noseConeMOI", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


####BodyTube
@app.route('/BodyTube/calculateCG', methods=['POST'])
def calculateBodyTubeCG():
    app.logger.info(f"{request.json}")
    try:
        cg = body_tube_cg_helper.calculateCG(request.json)
        error_file_path = "/data/workspace/myshixun/bodyTubeCG/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cg, request.json['answer'], os.path.join(os.getcwd(), "bodyTubeCG"))
        return {"code": 200, "msg": "ok", "result": cg}
    except Exception:
        with open(os.path.join("bodyTubeCG", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/BodyTube/calculateCP', methods=['POST'])
def calculateBodyTubeCP():
    app.logger.info(f"{request.json}")
    try:
        cp = body_tube_cp_helper.calculateCP(request.json)
        error_file_path = "/data/workspace/myshixun/bodyTubeCP/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cp, request.json['answer'], os.path.join(os.getcwd(), "bodyTubeCP"))
        return {"code": 200, "msg": "ok", "result": cp}
    except Exception:
        with open(os.path.join("bodyTubeCP", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/BodyTube/calculateMOI', methods=['POST'])
def calculateBodyTubeMOI():
    app.logger.info(f"{request.json}")
    try:
        moi = body_tube_moi_helper.calculate_moi(request.json)
        error_file_path = "/data/workspace/myshixun/bodyTubeMOI/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(moi, request.json['answer'], os.path.join(os.getcwd(), "bodyTubeMOI"))
        return {"code": 200, "msg": "ok", "result": moi}
    except Exception:
        with open(os.path.join("bodyTubeMOI", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


# Finset
@app.route('/FinSet/calculateCG', methods=['POST'])
def calculateFinSetCG():
    app.logger.info(f"{request.json}")
    try:
        cg = finset_cg_helper.calculateCG(request.json)
        error_file_path = "/data/workspace/myshixun/finSetCG/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cg, request.json['answer'], os.path.join(os.getcwd(), "finSetCG"))
        return {"code": 200, "msg": "ok", "result": cg}
    except Exception:
        with open(os.path.join("finSetCG", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/FinSet/calculateCP', methods=['POST'])
def calculateFinSetCP():
    app.logger.info(f"{request.json}")
    try:
        cp = finset_cp_helper.calculateCP(request.json)
        error_file_path = "/data/workspace/myshixun/finSetCP/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cp, request.json['answer'], os.path.join(os.getcwd(), "finSetCP"))
        return {"code": 200, "msg": "ok", "result": cp}
    except Exception:
        with open(os.path.join("finSetCP", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/FinSet/calculateMOI', methods=['POST'])
def calculateFinSetMOI():
    app.logger.info(f"{request.json}")
    try:
        moi = finset_moi_helper.calculate_moi(request.json)
        error_file_path = "/data/workspace/myshixun/finSetMOI/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(moi, request.json['answer'], os.path.join(os.getcwd(), "finSetMOI"))
        return {"code": 200, "msg": "ok", "result": moi}
    except Exception:
        with open(os.path.join("finSetMOI", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


# Transition
@app.route('/Transition/calculateCG', methods=['POST'])
def calculateTransitionCG():
    app.logger.info(f"{request.json}")
    try:
        cg = transition_cg_helper.calculateCG(request.json)
        error_file_path = "/data/workspace/myshixun/transitionCG/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cg, request.json['answer'], os.path.join(os.getcwd(), "transitionCG"))
        return {"code": 200, "msg": "ok", "result": cg}
    except Exception:
        with open(os.path.join("transitionCG", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/Transition/calculateCP', methods=['POST'])
def calculateTransitionCP():
    app.logger.info(f"{request.json}")
    try:
        cp = transition_cp_helper.calculateCP(request.json)
        error_file_path = "/data/workspace/myshixun/transitionCP/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cp, request.json['answer'], os.path.join(os.getcwd(), "transitionCP"))
        return {"code": 200, "msg": "ok", "result": cp}
    except Exception:
        with open(os.path.join("transitionCP", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/Transition/calculateMOI', methods=['POST'])
def calculateTransitionMOI():
    app.logger.info(f"{request.json}")
    try:
        moi = transition_moi_helper.calculateMOI(request.json)
        error_file_path = "/data/workspace/myshixun/transitionMOI/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(moi, request.json['answer'], os.path.join(os.getcwd(), "transitionMOI"))
        return {"code": 200, "msg": "ok", "result": moi}
    except Exception:
        with open(os.path.join("transitionMOI", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/TubeFinSet/calculateCG', methods=['POST'])
def calculateTubeFinSetCG():
    app.logger.info(f"{request.json}")
    try:
        cg = tube_fine_set_cg_helper.calculateCG(request.json)
        error_file_path = "/data/workspace/myshixun/tubeFinsetCG/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cg, request.json['answer'], os.path.join(os.getcwd(), "tubeFinsetCG"))
        return {"code": 200, "msg": "ok", "result": cg}
    except Exception:
        with open(os.path.join("tubeFinsetCG", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/TubeFinSet/calculateCP', methods=['POST'])
def calculateTubeFinSetCP():
    app.logger.info(f"{request.json}")
    try:
        cp = tube_fine_set_cp_helper.calculateCP(request.json)
        error_file_path = "/data/workspace/myshixun/tubeFinsetCP/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cp, request.json['answer'], os.path.join(os.getcwd(), "tubeFinsetCP"))
        return {"code": 200, "msg": "ok", "result": cp}
    except Exception:
        with open(os.path.join("tubeFinsetCP", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/TubeFinSet/calculateMOI', methods=['POST'])
def calculateTubeFinSetMOI():
    app.logger.info(f"{request.json}")
    try:
        moi = tube_fine_set_moi_helper.calculateMOI(request.json)
        error_file_path = "/data/workspace/myshixun/tubeFinsetMOI/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(moi, request.json['answer'], os.path.join(os.getcwd(), "tubeFinsetMOI"))
        return {"code": 200, "msg": "ok", "result": moi}
    except Exception:
        with open(os.path.join("tubeFinsetMOI", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/LaunchLug/calculateCG', methods=['POST'])
def calculateLaunchLugCG():
    app.logger.info(f"{request.json}")
    try:
        cg = launchlug_cg_helper.calculateCG(request.json)
        error_file_path = "/data/workspace/myshixun/launchLugCG/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cg, request.json['answer'], os.path.join(os.getcwd(), "launchLugCG"))
        return {"code": 200, "msg": "ok", "result": cg}
    except Exception:
        with open(os.path.join("launchLugCG", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/LaunchLug/calculateCP', methods=['POST'])
def calculateLaunchLugCP():
    app.logger.info(f"{request.json}")
    try:
        cp = launchlug_cp_helper.calculateCP(request.json)
        error_file_path = "/data/workspace/myshixun/launchLugCP/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cp, request.json['answer'], os.path.join(os.getcwd(), "launchLugCP"))
        return {"code": 200, "msg": "ok", "result": cp}
    except Exception:
        with open(os.path.join("launchLugCP", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/LaunchLug/calculateMOI', methods=['POST'])
def calculateLaunchLugMOI():
    app.logger.info(f"{request.json}")
    try:
        moi = launchlug_moi_helper.calculateMOI(request.json)
        error_file_path = "/data/workspace/myshixun/launchLugMOI/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(moi, request.json['answer'], os.path.join(os.getcwd(), "launchLugMOI"))
        return {"code": 200, "msg": "ok", "result": moi}
    except Exception:
        with open(os.path.join("launchLugMOI", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/RailButton/calculateCG', methods=['POST'])
def calculateRailButtonCG():
    app.logger.info(f"{request.json}")
    try:
        cg = rail_button_cg_helper.calculateCG(request.json)
        error_file_path = "/data/workspace/myshixun/railButtonCG/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cg, request.json['answer'], os.path.join(os.getcwd(), "railButtonCG"))
        return {"code": 200, "msg": "ok", "result": cg}
    except Exception:
        with open(os.path.join("railButtonCG", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/RailButton/calculateCP', methods=['POST'])
def calculateRailButtonCP():
    app.logger.info(f"{request.json}")
    try:
        cp = rail_button_cp_helper.calculateCP(request.json)
        error_file_path = "/data/workspace/myshixun/railButtonCP/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cp, request.json['answer'], os.path.join(os.getcwd(), "railButtonCP"))
        return {"code": 200, "msg": "ok", "result": cp}
    except Exception:
        with open(os.path.join("railButtonCP", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/RailButton/calculateMOI', methods=['POST'])
def calculateRailButtonMOI():
    app.logger.info(f"{request.json}")
    try:
        moi = rail_button_moi_helper.calculateMOI(request.json)
        error_file_path = "/data/workspace/myshixun/railButtonMOI/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(moi, request.json['answer'], os.path.join(os.getcwd(), "railButtonMOI"))
        return {"code": 200, "msg": "ok", "result": moi}
    except Exception:
        with open(os.path.join("railButtonMOI", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/InnerTube/calculateCG', methods=['POST'])
def calculateInnerTubeCG():
    app.logger.info(f"{request.json}")
    try:
        cg = inner_tube_cg_helper.calculateCG(request.json)
        error_file_path = "/data/workspace/myshixun/innerTubeCG/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cg, request.json['answer'], os.path.join(os.getcwd(), "innerTubeCG"))
        return {"code": 200, "msg": "ok", "result": cg}
    except Exception:
        with open(os.path.join("innerTubeCG", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/InnerTube/calculateMOI', methods=['POST'])
def calculateInnerTubeMOI():
    app.logger.info(f"{request.json}")
    try:
        moi = inner_tube_moi_helper.calculateMOI(request.json)
        error_file_path = "/data/workspace/myshixun/innerTubeMOI/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(moi, request.json['answer'], os.path.join(os.getcwd(), "innerTubeMOI"))
        return {"code": 200, "msg": "ok", "result": moi}
    except Exception:
        with open(os.path.join("innerTubeMOI", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/InnerComponent/calculateCG', methods=['POST'])
def calculateInnerComponentCG():
    app.logger.info(f"{request.json}")
    try:
        cg = inner_component_cg_helper.calculateCG(request.json)
        error_file_path = "/data/workspace/myshixun/innerComponentCG/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cg, request.json['answer'], os.path.join(os.getcwd(), "innerComponentCG"))
        return {"code": 200, "msg": "ok", "result": cg}
    except Exception:
        with open(os.path.join("innerComponentCG", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/InnerComponent/calculateMOI', methods=['POST'])
def calculateInnerComponentMOI():
    app.logger.info(f"{request.json}")
    try:
        moi = inner_component_moi_helper.calculateMOI(request.json)
        error_file_path = "/data/workspace/myshixun/innerComponentMOI/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(moi, request.json['answer'], os.path.join(os.getcwd(), "innerComponentMOI"))
        return {"code": 200, "msg": "ok", "result": moi}
    except Exception:
        with open(os.path.join("innerComponentMOI", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/Parachute/calculateCG', methods=['POST'])
def calculateParachuteCG():
    app.logger.info(f"{request.json}")
    try:
        cg = parachute_cg_helper.calculateCG(request.json)
        error_file_path = "/data/workspace/myshixun/parachuteCG/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cg, request.json['answer'], os.path.join(os.getcwd(), "parachuteCG"))
        return {"code": 200, "msg": "ok", "result": cg}
    except Exception:
        with open(os.path.join("parachuteCG", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/Parachute/calculateMOI', methods=['POST'])
def calculateParachuteMOI():
    app.logger.info(f"{request.json}")
    try:
        moi = parachute_moi_helper.calculateMOI(request.json)
        error_file_path = "/data/workspace/myshixun/parachuteMOI/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(moi, request.json['answer'], os.path.join(os.getcwd(), "parachuteMOI"))
        return {"code": 200, "msg": "ok", "result": moi}
    except Exception:
        with open(os.path.join("parachuteMOI", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/ShockCord/calculateCG', methods=['POST'])
def calculateShockCordCG():
    app.logger.info(f"{request.json}")
    try:
        cg = shockCord_cg_helper.calculateCG(request.json)
        error_file_path = "/data/workspace/myshixun/shockCordCG/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cg, request.json['answer'], os.path.join(os.getcwd(), "shockCordCG"))
        return {"code": 200, "msg": "ok", "result": cg}
    except Exception:
        with open(os.path.join("shockCordCG", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/ShockCord/calculateMOI', methods=['POST'])
def calculateShockCordMOI():
    app.logger.info(f"{request.json}")
    try:
        moi = shockCord_moi_helper.calculateMOI(request.json)
        error_file_path = "/data/workspace/myshixun/shockCordMOI/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(moi, request.json['answer'], os.path.join(os.getcwd(), "shockCordMOI"))
        return {"code": 200, "msg": "ok", "result": moi}
    except Exception:
        with open(os.path.join("shockCordMOI", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/MassComponent/calculateCG', methods=['POST'])
def calculateMassComponentCG():
    app.logger.info(f"{request.json}")
    try:
        cg = mass_component_cg_helper.calculateCG(request.json)
        error_file_path = "/data/workspace/myshixun/massComponentCG/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cg, request.json['answer'], os.path.join(os.getcwd(), "massComponentCG"))
        return {"code": 200, "msg": "ok", "result": cg}
    except Exception:
        with open(os.path.join("massComponentCG", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/MassComponent/calculateMOI', methods=['POST'])
def calculateMassComponentMOI():
    app.logger.info(f"{request.json}")
    try:
        moi = mass_component_moi_helper.calculateMOI(request.json)
        error_file_path = "/data/workspace/myshixun/massComponentMOI/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(moi, request.json['answer'], os.path.join(os.getcwd(), "massComponentMOI"))
        return {"code": 200, "msg": "ok", "result": moi}
    except Exception:
        with open(os.path.join("massComponentMOI", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/Streamer/calculateCG', methods=['POST'])
def calculateStreamerCG():
    app.logger.info(f"{request.json}")
    try:
        cg = streamer_cg_helper.calculateCG(request.json)
        error_file_path = "/data/workspace/myshixun/streamerCG/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cg, request.json['answer'], os.path.join(os.getcwd(), "streamerCG"))
        return {"code": 200, "msg": "ok", "result": cg}
    except Exception:
        with open(os.path.join("streamerCG", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/Streamer/calculateMOI', methods=['POST'])
def calculateStreamerMOI():
    app.logger.info(f"{request.json}")
    try:
        moi = streamer_moi_helper.calculateMOI(request.json)
        error_file_path = "/data/workspace/myshixun/streamerMOI/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(moi, request.json['answer'], os.path.join(os.getcwd(), "streamerMOI"))
        return {"code": 200, "msg": "ok", "result": moi}
    except Exception:
        with open(os.path.join("streamerMOI", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/Stage/calculateCG', methods=['POST'])
def calculateStageCG():
    app.logger.info(f"{request.json}")
    try:
        cg = stage_cg_helper.calculateCG(request.json)
        error_file_path = "/data/workspace/myshixun/stageCG/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cg, request.json['answer'], os.path.join(os.getcwd(), "stageCG"))
        return {"code": 200, "msg": "ok", "result": cg}
    except Exception:
        with open(os.path.join("stageCG", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/Stage/calculateCP', methods=['POST'])
def calculateStageCP():
    app.logger.info(f"{request.json}")
    try:
        cp = stage_cp_helper.calculateCP(request.json)
        error_file_path = "/data/workspace/myshixun/stageCP/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cp, request.json['answer'], os.path.join(os.getcwd(), "stageCP"))
        return {"code": 200, "msg": "ok", "result": cp}
    except Exception:
        with open(os.path.join("stageCP", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/Stage/calculateMOI', methods=['POST'])
def calculateStageMOI():
    app.logger.info(f"{request.json}")
    try:
        moi = stage_moi_helper.calculateMOI(request.json)
        error_file_path = "/data/workspace/myshixun/stageMOI/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(moi, request.json['answer'], os.path.join(os.getcwd(), "stageMOI"))
        return {"code": 200, "msg": "ok", "result": moi}
    except Exception:
        with open(os.path.join("stageMOI", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/Pods/calculateCG', methods=['POST'])
def calculatePodsCG():
    app.logger.info(f"{request.json}")
    try:
        cg = pods_cg_helper.calculateCG(request.json)
        error_file_path = "/data/workspace/myshixun/podsCG/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cg, request.json['answer'], os.path.join(os.getcwd(), "podsCG"))
        return {"code": 200, "msg": "ok", "result": cg}
    except Exception:
        with open(os.path.join("podsCG", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/Pods/calculateCP', methods=['POST'])
def calculatePodsCP():
    app.logger.info(f"{request.json}")
    try:
        cp = pods_cp_helper.calculateCP(request.json)
        error_file_path = "/data/workspace/myshixun/podsCP/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cp, request.json['answer'], os.path.join(os.getcwd(), "podsCP"))
        return {"code": 200, "msg": "ok", "result": cp}
    except Exception:
        with open(os.path.join("podsCP", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/Pods/calculateMOI', methods=['POST'])
def calculatePodsMOI():
    app.logger.info(f"{request.json}")
    try:
        moi = pods_moi_helper.calculateMOI(request.json)
        error_file_path = "/data/workspace/myshixun/podsMOI/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(moi, request.json['answer'], os.path.join(os.getcwd(), "podsMOI"))
        return {"code": 200, "msg": "ok", "result": moi}
    except Exception:
        with open(os.path.join("podsMOI", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/Whole/calculateCG', methods=['POST'])
def calculateWholeCG():
    app.logger.info(f"{request.json}")
    try:
        cg = whole_cg_helper.calculateCG(request.json)
        error_file_path = "/data/workspace/myshixun/wholeCG/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cg, request.json['answer'], os.path.join(os.getcwd(), "wholeCG"))
        return {"code": 200, "msg": "ok", "result": cg}
    except Exception:
        with open(os.path.join("wholeCG", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/Whole/calculateCP', methods=['POST'])
def calculateWholeCP():
    app.logger.info(f"{request.json}")
    try:
        cg = whole_cp_helper.calculateCP(request.json)
        error_file_path = "/data/workspace/myshixun/wholeCP/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cg, request.json['answer'], os.path.join(os.getcwd(), "wholeCP"))
        return {"code": 200, "msg": "ok", "result": cg}
    except Exception:
        with open(os.path.join("wholeCP", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/Whole/calculateMOI', methods=['POST'])
def calculateWholeMOI():
    app.logger.info(f"{request.json}")
    try:
        cg = whole_moi_helper.calculateMOI(request.json)
        error_file_path = "/data/workspace/myshixun/wholeMOI/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        check(cg, request.json['answer'], os.path.join(os.getcwd(), "wholeMOI"))
        return {"code": 200, "msg": "ok", "result": cg}
    except Exception:
        with open(os.path.join("wholeMOI", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/Motor/point', methods=['POST'])
def calculatePoint():
    app.logger.info(f"{request.json}")
    try:
        result = myPoint.point()
        with open(os.path.join("motorPoint", "result.txt"), 'w') as f:
            f.write("发动机推力曲线加载成功")
        return {"code": 200, "msg": "ok", "result": result}
    except Exception:
        with open(os.path.join("motorPoint", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/Motor/function', methods=['POST'])
def calculateFunction():
    app.logger.info(f"{request.json}")
    try:
        result = myFunction.functions()
        with open(os.path.join("motorFunction", "result.txt"), 'w') as f:
            f.write("发动机推力曲线加载成功")
        return {"code": 200, "msg": "ok", "result": result}
    except Exception:
        with open(os.path.join("motorFunction", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


# 计算对称组件的压差阻力
@app.route('/Projectile/calculatePressureCD', methods=['POST'])
def calculateBodyTubePressureCD():
    try:
        pcd = calculateSymPCDHelper.calculate(request.json)
        error_file_path = "/data/workspace/myshixun/step1/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)

        # check(cg, request.json['answer'], os.path.join(os.getcwd(), "step1"))
        return {"code": 200, "msg": "ok", "result": pcd}
    # 异常处理
    except Exception:
        with open(os.path.join("step1", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


# 计算总体基底阻力
@app.route('/Whole/cd', methods=['POST'])
def calculateCDs():
    try:
        timestamp = request.json.get('timestamp', None)
        result = calculateCD(request.json)
        # totalCDs.append({'timestamp': timestamp, 'result': result})
        # # totalCDs.sort(key=lambda x: x['timestamp'])
        return {"code": 200, "msg": "ok", "result": result}
    except Exception:
        with open(os.path.join("dir", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/Projectile/calculateFinsetPressureCD', methods=['POST'])
def calculateFinsetCDs():
    try:
        timestamp = request.json.get('timestamp', None)
        result = calculateFSPCD(request.json)
        return {"code": 200, "msg": "ok", "result": result}
    except Exception:
        with open(os.path.join("calculateFinsetPCD", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


# 轴向力系数计算

@app.route('/Projectile/calculateAxialCD', methods=['POST'])
def calculateAxialCD():
    try:
        timestamp = request.json.get('timestamp', None)
        result = calculateAixalCDHelper(request.json)
        return {"code": 200, "msg": "ok", "result": result}
    except Exception:
        with open(os.path.join("calculateFinsetPCD", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


# 摩擦阻力计算
@app.route('/Projectile/calculateFrictionCD', methods=['POST'])
def calculateFrictionCD():
    try:
        timestamp = request.json.get('timestamp', None)
        result = calculateFriectionCDHelper(request.json)
        return {"code": 200, "msg": "ok", "result": result}
    except Exception:
        with open(os.path.join("calculateFinsetPCD", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


wordCoordinates = []


@app.route('/Projectile/Acceleration', methods=['POST'])
def Acceleration():
    try:
        result = calculateAccelerationHelper(request.json)
        return {"code": 200, "msg": "ok", "result": result}
    except Exception:
        with open(os.path.join("calculateFinsetPCD", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


# plt 弹道轨迹三维图
@app.route('/Projectile/position', methods=['POST'])
def AccelerationPosition():
    try:
        plot_rocket_from_json(request.json)
        return {"code": 200, "msg": "ok", "result": 0}
    except Exception:
        with open(os.path.join("calculateFinsetPCD", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


# plt 总体压差图
@app.route('/Projectile/totalPressureCDPLT', methods=['POST'])
def totalpressurecdPLT():
    try:
        print("totalpressurecdPLT")
        totalpressureCD(request.json, "./calculateTotalPressureCD/image")
        return {"code": 200, "msg": "ok", "result": 0}
    except Exception:
        print(Exception)
        with open(os.path.join("calculateFinsetPCD", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


# plt  totalmoment图
@app.route('/Projectile/totalMomentPLT', methods=['POST'])
def totalMomentPLT():
    try:
        print("totalMomentPLT")
        totalMomentPLTImage(request.json, "./totalMoment/image")
        # plot_rocket_from_json(request.json)
        return {"code": 200, "msg": "ok", "result": 0}
    except Exception:
        with open(os.path.join("calculateFinsetPCD", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


# plt  总体法向力
@app.route('/Projectile/calculateComponentNonAxialForcesPLT', methods=['POST'])
def calculateComponentNonAxialForcesPLT():
    try:
        print("calculateComponentNonAxialForcesPLT")
        componentNonAxialForcePLT(request.json, "./calculatecomponentNonAxialForces/image")
        return {"code": 200, "msg": "ok", "result": 0}
    except Exception:
        with open(os.path.join("calculateFinsetPCD", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


# plt  calculateCNPLT 尾翼
@app.route('/Wing/calculateCNPLT', methods=['POST'])
def calculateCNPLT():
    try:
        print("calculateCNPLT")
        WingCNPLT(request.json, "./wingDemo/image")
        return {"code": 200, "msg": "ok", "result": 0}
    except Exception:
        with open(os.path.join("calculateFinsetPCD", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


# plt  稳定性
@app.route('/Projectile/calculateStabilityPLT', methods=['POST'])
def calculateStabilityPLT():
    try:
        print("calculateStabilityPLT")
        stabilityPLT(request.json, "./calculateStability/image")
        return {"code": 200, "msg": "ok", "result": 0}
    except Exception:
        with open(os.path.join("calculateFinsetPCD", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


# plt  AccelerationPLT
@app.route('/Projectile/AccelerationPLT', methods=['POST'])
def AccelerationPLT():
    try:
        print("AccelerationPLT")
        calculateAccelerationPLT(request.json, "./calculateAcceleration/image")
        return {"code": 200, "msg": "ok", "result": 0}
    except Exception:
        with open(os.path.join("calculateFinsetPCD", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


# plt  calculateFrictionCDPLT
@app.route('/Projectile/calculateFrictionCDPLT', methods=['POST'])
def calculateFrictionCDPLT():
    try:
        print("calculateFrictionCDPLT")
        frictionPLT(request.json, "./calculateFrictionCD/image")
        return {"code": 200, "msg": "ok", "result": 0}
    except Exception:
        with open(os.path.join("calculateFinsetPCD", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


# plt  calculateAxialCDPLT
@app.route('/Projectile/calculateAxialCDPLT', methods=['POST'])
def calculateAxialCDPLT():
    try:
        print("calculateAxialCDPLT")
        # plot_rocket_from_json(request.json)
        axialCDPLT(request.json, "./calculateAxialCD/image")
        return {"code": 200, "msg": "ok", "result": 0}
    except Exception:
        with open(os.path.join("calculateFinsetPCD", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


# plt  calculateFinsetPressureCDPLT
@app.route('/Projectile/calculateFinsetPressureCDPLT', methods=['POST'])
def calculateFinsetPressureCDPLT():
    try:
        print("calculateFinsetPressureCDPLT")
        finsetPressureCDPLT(request.json, "./calculateFinsetPCD/image")
        return {"code": 200, "msg": "ok", "result": 0}
    except Exception:
        with open(os.path.join("calculateFinsetPCD", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


# plt  calculateCDPLT
@app.route('/Projectile/calculateCDPLT', methods=['POST'])
def calculateCDPLT():
    try:
        print("calculateCDPLT")
        cdPLT(request.json, "./calculateCD/image")
        return {"code": 200, "msg": "ok", "result": 0}
    except Exception:
        with open(os.path.join("calculateFinsetPCD", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


# plt  calculatePressureCD  弹体的
@app.route('/Projectile/calculatePressureCDPLT', methods=['POST'])
def calculatePressureCD():
    try:
        print("calculatePressureCD")
        pressureCD(request.json, "./calculateSymComponentPCD/image")
        return {"code": 200, "msg": "ok", "result": 0}
    except Exception:
        with open(os.path.join("calculateFinsetPCD", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


# plt  calculateCNPLT1
@app.route('/Projectile/calculateCNPLT1', methods=['POST'])
def calculateCNPLT1():
    try:
        print("calculatePressureCD")
        hullCNPLT(request.json, "./calculateTubeFinSetHullCG/image")

        return {"code": 200, "msg": "ok", "result": 0}
    except Exception:
        with open(os.path.join("calculateFinsetPCD", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/Projectile/Stability', methods=['POST'])
def Stability():
    try:
        timestamp = request.json.get('timestamp', None)
        stab = calculateStabilityHelper(request.json)
        # stabilitys.append({'timestamp': timestamp, 'result': stab})
        # stabilitys.sort(key=lambda x: x['timestamp'])
        return {"code": 200, "msg": "ok", "result": stab}
    except Exception:
        with open(os.path.join("calculateFinsetPCD", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/Projectile/calculateCN', methods=['POST'])
def calculateCN():
    # app.logger.info(f"{request.json}")
    try:
        cg = demo.calculate(request.json)
        error_file_path = "/data/workspace/myshixun/step1/error.txt"
        if os.path.exists(error_file_path):
            os.remove(error_file_path)
        #     对比每一次的是否一致
        # check(cg, request.json['answer'], os.path.join(os.getcwd(), "step1"))
        return {"code": 200, "msg": "ok", "result": cg}
    # 异常处理
    except Exception:
        with open(os.path.join("step1", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return {"code": 500, "msg": "error", "result": traceback.format_exc()}


@app.route('/Projectile/checkJson', methods=['POST'])
def check_json_api():
    # 及时清除上次结果
    file_path = "/data/workspace/myshixun/result.txt"
    if os.path.exists(file_path):  # 检查文件是否存在
        os.remove(file_path)  # 删除文件
    data = request.json
    json_a = data.get('Client_List', {})
    json_b = data.get('Server_List', {})
    print(type(json_a[0]))
    print(json_a)
    print(json_b)

    # 原json比较
    # ret = check_json(json_a, json_b)
    # 新无序列表比较
    ret = check_list(json_a, json_b)
    if ret:
        print("success")
        with open(file_path, 'w+') as f:
            f.write("头歌实践教学平台欢迎你\n请认真作答\n比对成功")
    else:
        print("fail")
        # 计算最大误差
        max_error = 0
        for i in range(len(json_b)):
            if abs(json_a[i] - json_b[i]) > max_error:
                max_error = abs(json_a[i] - json_b[i])
        with open(file_path, 'w+') as f:
            f.write("头歌实践教学平台欢迎你\n请认真作答\n比对失败")
            f.write(f"n（最大误差：{max_error:.2f}%）\n")

    return jsonify({"code": 200, "msg": ret})


@app.route('/Projectile/checkJson2', methods=['POST'])
def check_json_api2():
    # 及时清除上次结果
    file_path = "/data/workspace/myshixun/result.txt"
    if os.path.exists(file_path):  # 检查文件是否存在
        os.remove(file_path)  # 删除文件
    data = request.json
    json_a = data.get('Client_List', {})
    json_a2 = data.get('Client_List2', {})
    json_b = data.get('Server_List', {})
    json_b2 = data.get('Server_List2', {})
    # 原json比较
    # ret = check_json(json_a, json_b)
    # 新无序列表比较
    ret = check_list2(json_a, json_b)
    ret2 = check_list2(json_a2, json_b2)
    if ret and ret2:
        print("success")
        with open(file_path, 'w+') as f:
            f.write("头歌实践教学平台欢迎你\n请认真作答\n比对成功")
    else:
        print("fail")
        max_error = 0
        max_error2 = 0
        for i in range(len(json_b)):
            if abs(json_a[i] - json_b[i]) > max_error:
                max_error = abs(json_a[i] - json_b[i])
        for i in range(len(json_b2)):
            if abs(json_a2[i] - json_b2[i]) > max_error2:
                max_error2 = abs(json_a2[i] - json_b2[i])
        with open(file_path, 'w+') as f:
            f.write("头歌实践教学平台欢迎你\n请认真作答\n比对失败")
            f.write(f"n（线性加速度最大误差：{max_error:.2f}%）\n")
            f.write(f"n（角加速度最大误差：{max_error2:.2f}%）\n")

    return jsonify({"code": 200, "msg": ret})


@app.route('/Projectile/checkJSON4', methods=['POST'])
def check_json_api4():
    # 及时清除上次结果
    file_path = "/data/workspace/myshixun/result.txt"
    if os.path.exists(file_path):  # 检查文件是否存在
        os.remove(file_path)  # 删除文件
    data = request.json
    json_a = data.get('Client_List1', {})
    json_b = data.get('Server_List1', {})
    json_a2 = data.get('Client_List2', {})
    json_b2 = data.get('Server_List2', {})
    json_a3 = data.get('Server_List3', {})
    json_b3 = data.get('Server_List3', {})
    # 原json比较
    # ret = check_json(json_a, json_b)
    # 新无序列表比较
    ret = check_list(json_a, json_b)
    ret2 = check_list(json_a2, json_b2)
    ret3 = check_list(json_a3, json_b3)
    if ret and ret2 and ret3:
        print("success")
        with open(file_path, 'w+') as f:
            f.write("头歌实践教学平台欢迎你\n请认真作答\n比对成功")
    else:
        print("fail")
        max_error = 0
        max_error2 = 0
        max_error3 = 0
        for i in range(len(json_b)):
            if abs(json_a[i] - json_b[i]) > max_error:
                max_error = abs(json_a[i] - json_b[i])
        for i in range(len(json_b2)):
            if abs(json_a2[i] - json_b2[i]) > max_error2:
                max_error2 = abs(json_a2[i] - json_b2[i])
        for i in range(len(json_b3)):
            if abs(json_a3[i] - json_b3[i]) > max_error3:
                max_error3 = abs(json_a3[i] - json_b3[i])
        with open(file_path, 'w+') as f:
            f.write("头歌实践教学平台欢迎你\n请认真作答\n比对失败")
            f.write(f"n（力矩最大误差：{max_error:.2f}%）\n")
            f.write(f"n（法向力最大误差：{max_error2:.2f}%）\n")
            f.write(f"n（滚转力矩最大误差：{max_error3:.2f}%）\n")
    return jsonify({"code": 200, "msg": ret})


@app.route('/Wing/calculateCN', methods=['POST'])
def wing_calculateCN_api():
    # print(request.json)
    app.logger.info(f"{request.json}")
    cna = calculate_nonaxial_forces(request.json)
    # print("edu",cna)
    return {"code": 200, "msg": "ok", "result": cna}


@app.route('/Projectile/totalMoment', methods=['POST'])
def calculateTotalMoment():
    app.logger.info(f"{request.json}")
    try:
        result = calculateTotalMomentHelper(request.json)
        print(result)
        return {"code": 200, "msg": "ok", "result": result}
    except Exception as e:
        print(traceback.format_exc())
        return jsonify({"code": 200, "msg": "error", "result": str(e)})


@app.route('/Projectile/totalPressureCD', methods=['POST'])
def calculateTotalCD():
    app.logger.info(f"{request.json}")
    try:
        result = calculatePressureCDHelper(request.json)
        return {"code": 200, "msg": "ok", "result": result}
    except Exception as e:
        print(Exception)
        return jsonify({"code": 200, "msg": "error", "result": str(e)})


@app.route('/Projectile/glideDistance', methods=['POST'])
def calculateglideDistance():
    app.logger.info(f"{request.json}")
    try:
        print("--------")
        print(request.json)
        result = calculateGlideDistancehelper(request.json)

        # answer
        height = request.json['height']
        c_l = request.json['c_l']
        c_d = request.json['c_d']

        answer = height / (1 / (c_l / c_d))
        # check
        if result == answer:
            print("success")
            text = "头歌实践教学平台欢迎你\n请认真作答\n比对成功"
        else:
            print("fail")
            text = "头歌实践教学平台欢迎你\n请认真作答\n比对失败"
        with open(os.path.join("calculateGlideDistance", "result.txt"), 'w') as f:
            print(text)
            f.write(text)
        return {"code": 200, "msg": "ok", "result": result}
    except Exception as e:
        error_message = ""
        if isinstance(e, ZeroDivisionError):
            error_message += "当前火箭无法分离，请检查火箭模型"
        with open(os.path.join("calculateGlideDistance", "error.txt"), 'w') as f:
            print(error_message)
            f.write(traceback.format_exc())
            f.write(error_message)

        return jsonify({"code": 200, "msg": "error", "result": 0})


@app.route('/Projectile/glideCharacter', methods=['POST'])
def calculateglideCharacter():
    app.logger.info(f"{request.json}")
    try:
        print("--------")
        print(request.json)
        result, result2 = calculateGlideCharacterhelper(request.json)

        # answer
        cl_max = request.json['max_cl']
        n_max = cl_max / request.json['w']
        refArea = request.json['refArea']
        w = request.json['w']
        # 标准空气密度
        p = 1.225
        V = math.sqrt((2 * n_max * request.json['w']) / (p * cl_max * refArea))
        answer = V * V / (9.8 * math.sqrt(n_max * n_max - 1))
        answer2 = 9.8 * math.sqrt(n_max * n_max - 1) / V
        # print(r)
        # check
        print(result)
        print(result2)
        print(answer)
        print(answer2)
        if result == answer and result2 == answer2:
            print("success")
            text = "头歌实践教学平台欢迎你\n请认真作答\n比对成功"
        else:
            print("fail")

            text = "头歌实践教学平台欢迎你\n请认真作答\n比对失败"
        with open(os.path.join("calculateGlideCharacter", "result.txt"), 'w') as f:
            f.write(text)
        return {"code": 200, "msg": "ok", "result": 0}
    except Exception as e:
        print("写入错误文档2....")
        with open(os.path.join("calculateGlideCharacter", "error.txt"), 'w') as f:
            f.write(traceback.format_exc())
        return jsonify({"code": 200, "msg": "error", "result": 0})


@app.route('/calculateComponentNonAxialForces', methods=['POST'])
def calculateComponentNonAxialForces_api():
    data = request.json
    app.logger.info(f"Received data: {data}")
    cn = addition_CN(data)
    if cn is None:
        return jsonify({"code": 400, "msg": "cn is None"}), 400
    return jsonify({"code": 200, "msg": "ok", "result": cn})


if __name__ == '__main__':
    app.run(host="127.0.0.1", port=8080, debug=True, threaded=True)
