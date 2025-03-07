import matplotlib.pyplot as plt
import os


def WingCNPLT(data, save_dir):
    # 绘制第一张图
    plt.figure()
    plt.plot(data)
    plt.title('figure1')
    plt.xlabel('x')
    plt.ylabel('y')
    # 保存第一张图
    plt.savefig(os.path.join(save_dir, 'figure1.png'))
    plt.close()
